package com.boondi.infrastructure.security;

import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client-IP rate limiting (E10-03), enforced with in-memory Bucket4j token buckets.
 *
 * Two tiers:
 *  - Auth endpoints (login/register/refresh/forgot/reset): {@value AUTH_REQUESTS_PER_MINUTE}/min —
 *    these are the brute-force / enumeration / email-spam surface, so the budget is deliberately
 *    tight but still comfortable for a human retrying a typoed password.
 *  - Everything else: {@value GENERAL_REQUESTS_PER_MINUTE}/min — high enough that legitimate
 *    clients (feed + badge polling + a burst of optimistic interactions) never see it.
 *
 * Buckets are per-instance (in-memory). If the backend ever scales horizontally, the limits
 * multiply by instance count unless this is swapped for Bucket4j's Redis-backed storage —
 * acceptable trade-off for the MVP's single-instance deploy, flagged here for later.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    static final int AUTH_REQUESTS_PER_MINUTE = 10;
    static final int GENERAL_REQUESTS_PER_MINUTE = 300;
    // Bound worst-case memory under an IP-spraying attack; clearing resets everyone's budget,
    // which is harmless (they just get a fresh minute) and far cheaper than LRU bookkeeping.
    private static final int MAX_TRACKED_BUCKETS = 50_000;

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // CORS preflights carry no credentials/payload and browsers control their cadence.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean authEndpoint = isAuthEndpoint(request);
        String key = (authEndpoint ? "auth:" : "api:") + clientIp(request);

        if (buckets.size() > MAX_TRACKED_BUCKETS) {
            buckets.clear();
        }
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(authEndpoint));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rate limit exceeded for {} at {}", key, request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(
                ErrorCode.RATE_LIMITED.name(),
                "Too many requests — please slow down and try again shortly",
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        // getRequestURI includes the /api/v1 context path; strip it so the check survives
        // context-path changes.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/auth/");
    }

    private String clientIp(HttpServletRequest request) {
        // Behind the nginx reverse proxy every request's remoteAddr is nginx itself, which
        // would collapse all clients into one bucket — prefer the forwarded client IP.
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket(boolean authEndpoint) {
        int perMinute = authEndpoint ? AUTH_REQUESTS_PER_MINUTE : GENERAL_REQUESTS_PER_MINUTE;
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(perMinute)
                        .refillGreedy(perMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
