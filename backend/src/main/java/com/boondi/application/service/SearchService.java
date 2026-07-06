package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.HashtagResponse;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.mapper.PostMapper;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.Hashtag;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.HashtagRepository;
import com.boondi.domain.repository.PostHashtagRepository;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Search backend (Epic 8). Users/hashtags use ILIKE/prefix matching; posts use the
 * `search_vector` tsvector column (V6 migration). All three paginate via a numeric
 * offset cursor, same pattern as TimelineService's trending endpoint — relevance/prefix
 * order isn't a stable time-based cursor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_LIMIT = 50;
    private static final int MAX_OFFSET = 500;
    private static final int TRENDING_HASHTAG_WINDOW_HOURS = 24;
    private static final int TRENDING_HASHTAG_COUNT = 10;
    private static final Duration TRENDING_HASHTAG_TTL = Duration.ofMinutes(10);
    private static final String TRENDING_HASHTAG_CACHE_KEY = "hashtags:trending";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostViewerStateService postViewerStateService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public CursorPage<UserResponse> searchUsers(String query, String cursorStr, int limit) {
        if (isBlank(query)) return emptyPage();

        int pageSize = clampLimit(limit);
        int offset = parseOffset(cursorStr);

        List<User> results = userRepository.searchUsers(query.trim(), pageSize + 1, offset);
        boolean hasMore = results.size() > pageSize && offset + pageSize < MAX_OFFSET;
        List<User> page = results.size() > pageSize ? results.subList(0, pageSize) : results;

        List<UserResponse> items = page.stream().map(userMapper::toResponse).toList();
        return buildPage(items, hasMore, offset, pageSize);
    }

    @Transactional(readOnly = true)
    public CursorPage<PostResponse> searchPosts(UUID viewerId, String query, String cursorStr, int limit) {
        if (isBlank(query)) return emptyPage();

        int pageSize = clampLimit(limit);
        int offset = parseOffset(cursorStr);

        List<Post> results = postRepository.searchPosts(query.trim(), pageSize + 1, offset);
        boolean hasMore = results.size() > pageSize && offset + pageSize < MAX_OFFSET;
        List<Post> page = results.size() > pageSize ? results.subList(0, pageSize) : results;

        List<PostResponse> items = page.stream().map(postMapper::toResponse).toList();
        postViewerStateService.enrich(viewerId, items);
        return buildPage(items, hasMore, offset, pageSize);
    }

    @Transactional(readOnly = true)
    public CursorPage<HashtagResponse> searchHashtags(String query, String cursorStr, int limit) {
        if (isBlank(query)) return emptyPage();

        int pageSize = clampLimit(limit);
        int offset = parseOffset(cursorStr);
        String normalized = query.trim().replaceFirst("^#", "").toLowerCase();

        List<Hashtag> results = hashtagRepository.searchByPrefix(normalized, pageSize + 1, offset);
        boolean hasMore = results.size() > pageSize && offset + pageSize < MAX_OFFSET;
        List<Hashtag> page = results.size() > pageSize ? results.subList(0, pageSize) : results;

        // Prefix search doesn't compute usage counts — only the trending endpoint does
        List<HashtagResponse> items = page.stream()
                .map(h -> HashtagResponse.builder().tag(h.getTag()).postCount(0).build())
                .toList();
        return buildPage(items, hasMore, offset, pageSize);
    }

    /** Top hashtags by usage in the last 24h (E8-05), Redis-cached for 10 minutes. */
    @Transactional(readOnly = true)
    public List<HashtagResponse> getTrendingHashtags() {
        Optional<List<HashtagResponse>> cached = readTrendingCache();
        if (cached.isPresent()) return cached.get();

        OffsetDateTime since = OffsetDateTime.now().minusHours(TRENDING_HASHTAG_WINDOW_HOURS);
        List<Object[]> rows = postHashtagRepository.findTrendingHashtags(
                since, PageRequest.of(0, TRENDING_HASHTAG_COUNT));

        List<HashtagResponse> result = rows.stream()
                .map(row -> HashtagResponse.builder()
                        .tag((String) row[0])
                        .postCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        writeTrendingCache(result);
        return result;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private boolean isBlank(String query) {
        return query == null || query.isBlank();
    }

    private int clampLimit(int requested) {
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    private int parseOffset(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return 0;
        try {
            return Math.min(Math.max(Integer.parseInt(cursorStr), 0), MAX_OFFSET);
        } catch (NumberFormatException e) {
            log.warn("Invalid search cursor '{}' — treating as first page", cursorStr);
            return 0;
        }
    }

    private <T> CursorPage<T> buildPage(List<T> items, boolean hasMore, int offset, int pageSize) {
        return CursorPage.<T>builder()
                .items(items)
                .nextCursor(hasMore ? String.valueOf(offset + pageSize) : null)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    private <T> CursorPage<T> emptyPage() {
        return CursorPage.<T>builder()
                .items(List.of())
                .nextCursor(null)
                .hasMore(false)
                .count(0)
                .build();
    }

    private Optional<List<HashtagResponse>> readTrendingCache() {
        try {
            String json = redisTemplate.opsForValue().get(TRENDING_HASHTAG_CACHE_KEY);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<List<HashtagResponse>>() {}));
        } catch (Exception e) {
            log.warn("Trending hashtags cache read failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void writeTrendingCache(List<HashtagResponse> result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(TRENDING_HASHTAG_CACHE_KEY, json, TRENDING_HASHTAG_TTL);
        } catch (Exception e) {
            log.warn("Trending hashtags cache write failed: {}", e.getMessage());
        }
    }
}
