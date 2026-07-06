package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.domain.repository.FollowRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis cache for the first page of home timelines.
 * Key: timeline:home:{userId} — TTL 5 minutes.
 * Invalidated for all followers when a followee posts or deletes a post,
 * and for the user themselves on follow/unfollow.
 * All Redis failures degrade gracefully to a DB query.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineCacheService {

    private static final String KEY_PREFIX = "timeline:home:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final FollowRepository followRepository;

    public Optional<CursorPage<PostResponse>> getHomeFirstPage(UUID userId) {
        try {
            String json = redisTemplate.opsForValue().get(key(userId));
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<CursorPage<PostResponse>>() {}));
        } catch (Exception e) {
            log.warn("Home timeline cache read failed for userId={}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public void cacheHomeFirstPage(UUID userId, CursorPage<PostResponse> page) {
        try {
            String json = objectMapper.writeValueAsString(page);
            redisTemplate.opsForValue().set(key(userId), json, TTL);
        } catch (Exception e) {
            log.warn("Home timeline cache write failed for userId={}: {}", userId, e.getMessage());
        }
    }

    public void evictHome(UUID userId) {
        try {
            redisTemplate.delete(key(userId));
        } catch (Exception e) {
            log.warn("Home timeline cache evict failed for userId={}: {}", userId, e.getMessage());
        }
    }

    /** Evict the home timeline cache of everyone who follows the given author. */
    public void evictFollowersOf(UUID authorId) {
        List<UUID> followerIds = followRepository.findFollowerIds(authorId);
        for (UUID followerId : followerIds) {
            evictHome(followerId);
        }
    }

    private String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
