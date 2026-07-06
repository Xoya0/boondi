package com.boondi.presentation.controller;

import com.boondi.application.dto.request.LoginRequest;
import com.boondi.application.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("boondi_test")
            .withUsername("boondi")
            .withPassword("boondi");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        registry.add("app.jwt.secret", () ->
                "test-secret-key-for-ci-pipeline-must-be-256-bits-long-for-hmac-sha-algorithm");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("Password123!")
                .displayName("Test User")
                .build();
    }

    @Test
    @DisplayName("POST /auth/register - 201 Created with tokens when valid request")
    void register_success() throws Exception {
        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)));

        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.expiresIn", greaterThan(0)))
                .andExpect(jsonPath("$.data.user.username", is("testuser")))
                .andExpect(jsonPath("$.data.user.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.data.user.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/register - 409 Conflict when email already registered")
    void register_duplicateEmail_conflict() throws Exception {
        // First registration
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        // Attempt registration with same email, different username
        RegisterRequest duplicateEmail = RegisterRequest.builder()
                .username("differentuser")
                .email("testuser@example.com")  // same email
                .password("Password123!")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("EMAIL_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/login - 200 OK with tokens when valid credentials")
    void login_success() throws Exception {
        // Register first (using a unique email to avoid conflicts with other tests)
        RegisterRequest loginTestUser = RegisterRequest.builder()
                .username("logintest")
                .email("logintest@example.com")
                .password("Password123!")
                .displayName("Login Test")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginTestUser)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("logintest@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.email", is("logintest@example.com")));
    }

    @Test
    @DisplayName("POST /auth/login - 401 Unauthorized when wrong password")
    void login_wrongPassword_unauthorized() throws Exception {
        // Register first
        RegisterRequest wrongPwdUser = RegisterRequest.builder()
                .username("wrongpwduser")
                .email("wrongpwd@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPwdUser)))
                .andExpect(status().isCreated());

        // Login with wrong password
        LoginRequest badLogin = LoginRequest.builder()
                .email("wrongpwd@example.com")
                .password("WrongPassword999!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("INVALID_CREDENTIALS")));
    }

    @Test
    @DisplayName("POST /auth/register - 400 Bad Request when invalid fields")
    void register_invalidRequest_badRequest() throws Exception {
        RegisterRequest invalid = RegisterRequest.builder()
                .username("ab")       // too short
                .email("not-an-email")
                .password("short")    // too short
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors", notNullValue()));
    }
}
