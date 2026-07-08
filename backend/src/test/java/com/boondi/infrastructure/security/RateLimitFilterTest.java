package com.boondi.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        // JSR-310 module registered, like the Spring-managed mapper injected in production
        // (ApiResponse.timestamp is a LocalDateTime).
        filter = new RateLimitFilter(new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    private MockHttpServletRequest request(String path, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1" + path);
        request.setContextPath("/api/v1");
        request.setRemoteAddr(ip);
        return request;
    }

    private MockHttpServletResponse fire(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void authEndpoint_allowsBudget_thenReturns429WithRateLimitedBody() throws Exception {
        for (int i = 0; i < RateLimitFilter.AUTH_REQUESTS_PER_MINUTE; i++) {
            assertThat(fire(request("/auth/login", "1.2.3.4")).getStatus()).isEqualTo(200);
        }

        MockHttpServletResponse blocked = fire(request("/auth/login", "1.2.3.4"));
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getContentAsString()).contains("RATE_LIMITED");
    }

    @Test
    void authBudget_isPerIp() throws Exception {
        for (int i = 0; i < RateLimitFilter.AUTH_REQUESTS_PER_MINUTE; i++) {
            fire(request("/auth/login", "1.2.3.4"));
        }

        // A different client is unaffected by the first client's exhausted bucket.
        assertThat(fire(request("/auth/login", "5.6.7.8")).getStatus()).isEqualTo(200);
    }

    @Test
    void nonAuthEndpoints_useTheLargerGeneralBudget() throws Exception {
        // More requests than the auth budget must still pass on a general endpoint.
        for (int i = 0; i < RateLimitFilter.AUTH_REQUESTS_PER_MINUTE * 2; i++) {
            assertThat(fire(request("/timelines/latest", "1.2.3.4")).getStatus()).isEqualTo(200);
        }
    }

    @Test
    void forwardedHeader_identifiesClientBehindProxy() throws Exception {
        // Behind nginx every remoteAddr is the proxy — X-Forwarded-For must win, otherwise
        // all clients share one bucket.
        for (int i = 0; i < RateLimitFilter.AUTH_REQUESTS_PER_MINUTE; i++) {
            MockHttpServletRequest request = request("/auth/login", "10.0.0.1");
            request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
            fire(request);
        }

        MockHttpServletRequest sameClient = request("/auth/login", "10.0.0.1");
        sameClient.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        assertThat(fire(sameClient).getStatus()).isEqualTo(429);

        MockHttpServletRequest otherClient = request("/auth/login", "10.0.0.1");
        otherClient.addHeader("X-Forwarded-For", "198.51.100.9, 10.0.0.1");
        assertThat(fire(otherClient).getStatus()).isEqualTo(200);
    }

    @Test
    void optionsPreflight_isNeverRateLimited() throws Exception {
        for (int i = 0; i < RateLimitFilter.AUTH_REQUESTS_PER_MINUTE * 2; i++) {
            MockHttpServletRequest preflight = request("/auth/login", "1.2.3.4");
            preflight.setMethod("OPTIONS");
            assertThat(fire(preflight).getStatus()).isEqualTo(200);
        }
    }
}
