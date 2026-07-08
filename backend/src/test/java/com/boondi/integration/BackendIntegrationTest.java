package com.boondi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests (E10-05) against a real Postgres + Redis via TestContainers,
 * exercising the full filter chain (JWT auth, rate limiting) and the JPQL that only breaks
 * on real Postgres.
 *
 * Every logical client gets a unique X-Forwarded-For so tests don't drain each other's
 * rate-limit buckets (the auth budget is only 10/min/IP and MockMvc's remoteAddr is
 * constant otherwise).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BackendIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(1);

    /** Unique per call — isolates each logical client's rate-limit bucket. */
    private static String nextIp() {
        return "203.0.113." + IP_COUNTER.getAndIncrement();
    }

    private record TestUser(String ip, String accessToken, String username) {}

    private TestUser registerUser() throws Exception {
        String ip = nextIp();
        String username = "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String body = """
                {"username":"%s","email":"%s@example.com","password":"password123"}
                """.formatted(username, username);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                // saveAndFlush regression: createdAt must be real in the immediate response
                .andExpect(jsonPath("$.data.user.createdAt").exists())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return new TestUser(ip, data.get("accessToken").asText(), username);
    }

    private String createPost(TestUser user, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .header("X-Forwarded-For", user.ip())
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();
    }

    // ---- Auth flow ----

    @Test
    void register_then_login_roundTrip() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(post("/auth/login")
                        .header("X-Forwarded-For", user.ip())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s@example.com\",\"password\":\"password123\"}"
                                .formatted(user.username())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void login_withWrongPassword_returns401InvalidCredentials() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(post("/auth/login")
                        .header("X-Forwarded-For", user.ip())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s@example.com\",\"password\":\"wrong-password\"}"
                                .formatted(user.username())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void register_withMalformedJson_returns400NotA500() throws Exception {
        // E10-01 regression: unparseable bodies used to fall into the generic 500 handler.
        mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401Not403() throws Exception {
        // Regression for the missing AuthenticationEntryPoint (post-Sprint-9 fix #4):
        // clients only refresh tokens on 401, so 403 here silently killed token refresh.
        mockMvc.perform(get("/timelines/home").header("X-Forwarded-For", nextIp()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withNonAdminToken_returns403() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(get("/admin/users")
                        .header("X-Forwarded-For", user.ip())
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    // ---- Posts + timelines (the null-cursor JPQL only fails on real Postgres) ----

    @Test
    void latestTimeline_firstPage_nullCursor_returns200WithCreatedPost() throws Exception {
        TestUser user = registerUser();
        String postId = createPost(user, "integration timeline post");

        // Regression for the pgjdbc null-cursor bug: first-page loads (cursor=null) failed
        // with "could not determine data type of parameter" before the cast() fix.
        MvcResult result = mockMvc.perform(get("/timelines/latest")
                        .header("X-Forwarded-For", user.ip()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains(postId);
    }

    @Test
    void homeTimeline_firstPage_returns200ForAuthenticatedUser() throws Exception {
        TestUser user = registerUser();

        mockMvc.perform(get("/timelines/home")
                        .header("X-Forwarded-For", user.ip())
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void replyFlow_incrementsParentReplyCount_andListsReply() throws Exception {
        TestUser user = registerUser();
        String parentId = createPost(user, "parent post");

        mockMvc.perform(post("/posts")
                        .header("X-Forwarded-For", user.ip())
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"the reply\",\"parentPostId\":\"" + parentId + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/posts/" + parentId)
                        .header("X-Forwarded-For", user.ip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replyCount").value(1));

        mockMvc.perform(get("/posts/" + parentId + "/replies")
                        .header("X-Forwarded-For", user.ip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content").value("the reply"));
    }

    @Test
    void likeFlow_updatesCountAndViewerFlag_andDuplicateIs409() throws Exception {
        TestUser author = registerUser();
        TestUser liker = registerUser();
        String postId = createPost(author, "like me");

        mockMvc.perform(post("/posts/" + postId + "/like")
                        .header("X-Forwarded-For", liker.ip())
                        .header("Authorization", "Bearer " + liker.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.likedByViewer").value(true));

        mockMvc.perform(post("/posts/" + postId + "/like")
                        .header("X-Forwarded-For", liker.ip())
                        .header("Authorization", "Bearer " + liker.accessToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_LIKED"));
    }

    @Test
    void interactingWithSoftDeletedPost_returns404() throws Exception {
        TestUser author = registerUser();
        TestUser other = registerUser();
        String postId = createPost(author, "soon deleted");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/posts/" + postId)
                        .header("X-Forwarded-For", author.ip())
                        .header("Authorization", "Bearer " + author.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/posts/" + postId + "/like")
                        .header("X-Forwarded-For", other.ip())
                        .header("Authorization", "Bearer " + other.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("POST_NOT_FOUND"));
    }

    // ---- Rate limiting (E10-03) ----

    @Test
    void authEndpoint_burst_returns429AfterBudgetExhausted() throws Exception {
        String ip = nextIp();
        int lastStatus = 0;
        for (int i = 0; i < 11; i++) {
            lastStatus = mockMvc.perform(post("/auth/login")
                            .header("X-Forwarded-For", ip)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"nobody@example.com\",\"password\":\"x\"}"))
                    .andReturn().getResponse().getStatus();
        }
        assertThat(lastStatus).isEqualTo(429);
    }
}
