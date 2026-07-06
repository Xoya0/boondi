package com.boondi.application.service;

import com.boondi.application.dto.request.LogoutRequest;
import com.boondi.application.dto.request.RefreshTokenRequest;
import com.boondi.application.dto.response.AuthResponse;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Validates the refresh token, issues a new token pair (rotation), and returns AuthResponse.
     */
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String rawRefreshToken = request.getRefreshToken();

        // 1. Validate JWT structure and expiry
        if (!jwtTokenProvider.isTokenValid(rawRefreshToken)) {
            throw BoondiException.tokenInvalid();
        }

        // 2. Must be a refresh token
        String tokenType = jwtTokenProvider.extractTokenType(rawRefreshToken);
        if (!"refresh".equals(tokenType)) {
            throw BoondiException.tokenInvalid();
        }

        // 3. Extract userId
        String userId = jwtTokenProvider.extractUserId(rawRefreshToken);
        String redisKey = "refresh:" + userId;

        // 4. Compare with stored token in Redis
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(rawRefreshToken)) {
            throw BoondiException.tokenInvalid();
        }

        // 5. Load user
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> BoondiException.userNotFound(userId));

        if (user.isSuspended()) {
            throw BoondiException.accountSuspended();
        }

        // 6. Generate new token pair
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 7. Rotate: overwrite Redis entry with new refresh token
        long ttlSeconds = jwtTokenProvider.getRefreshTokenExpiryMs() / 1000;
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, ttlSeconds, TimeUnit.SECONDS);

        log.info("Token refreshed for userId={}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Revokes the refresh token and blacklists the current access token.
     */
    public void logout(UUID userId, String rawAccessToken, LogoutRequest request) {
        // 1. Delete refresh token from Redis
        String refreshKey = "refresh:" + userId;
        redisTemplate.delete(refreshKey);
        log.info("Refresh token revoked for userId={}", userId);

        // 2. Optionally also delete via provided refresh token (belt-and-suspenders)
        if (StringUtils.hasText(request != null ? request.getRefreshToken() : null)) {
            // Already handled above — both point to same Redis key per user
        }

        // 3. Blacklist the access token
        if (StringUtils.hasText(rawAccessToken)) {
            try {
                String tokenHash = sha256Hex(rawAccessToken);
                String blacklistKey = "blacklist:access:" + tokenHash;

                // Calculate remaining TTL of access token
                Date expiry = jwtTokenProvider.extractAllClaims(rawAccessToken).getExpiration();
                long remainingSeconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;

                if (remainingSeconds > 0) {
                    redisTemplate.opsForValue().set(blacklistKey, "1", remainingSeconds, TimeUnit.SECONDS);
                    log.info("Access token blacklisted for userId={}, TTL={}s", userId, remainingSeconds);
                }
            } catch (Exception e) {
                // Token may already be expired — that's fine
                log.debug("Could not blacklist access token: {}", e.getMessage());
            }
        }
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
