package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.mapper.PostMapper;
import com.boondi.domain.entity.Post;
import com.boondi.domain.repository.PostRepository;
import com.boondi.infrastructure.exception.BoondiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int TRENDING_WINDOW_HOURS = 24;
    private static final int TRENDING_MAX_OFFSET = 200;

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostViewerStateService postViewerStateService;
    private final TimelineCacheService timelineCacheService;

    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getLatestTimeline(UUID viewerId, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);
        List<Post> posts = postRepository.findLatestTimeline(cursor, PageRequest.of(0, pageSize + 1));
        return buildPage(posts, pageSize, viewerId);
    }

    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getHomeTimeline(UUID userId, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);

        // Only the default first page is cached — that's what every session loads first
        boolean cacheable = cursor == null && pageSize == DEFAULT_LIMIT;
        if (cacheable) {
            Optional<CursorPage<PostResponse>> cached = timelineCacheService.getHomeFirstPage(userId);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        List<Post> posts = postRepository.findHomeTimeline(userId, cursor, PageRequest.of(0, pageSize + 1));
        CursorPage<PostResponse> page = buildPage(posts, pageSize, userId);

        if (cacheable) {
            timelineCacheService.cacheHomeFirstPage(userId, page);
        }
        return page;
    }

    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getUserTimeline(UUID viewerId, String username, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);
        List<Post> posts = postRepository.findUserTimeline(username, cursor, PageRequest.of(0, pageSize + 1));
        return buildPage(posts, pageSize, viewerId);
    }

    /**
     * Trending — last 24h, score = (likes × 1) + (reposts × 2).
     * Score ordering isn't a stable cursor, so the cursor here is a numeric offset.
     */
    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getTrendingTimeline(UUID viewerId, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        int offset = parseOffset(cursorStr);
        OffsetDateTime since = OffsetDateTime.now().minusHours(TRENDING_WINDOW_HOURS);

        List<Post> raw = postRepository.findTrending(since, PageRequest.of(0, offset + pageSize + 1));
        List<Post> window = raw.size() > offset ? raw.subList(offset, raw.size()) : List.of();

        boolean hasMore = window.size() > pageSize && offset + pageSize < TRENDING_MAX_OFFSET;
        List<Post> page = window.size() > pageSize ? window.subList(0, pageSize) : window;

        List<PostResponse> items = page.stream().map(postMapper::toResponse).toList();
        postViewerStateService.enrich(viewerId, items);

        return CursorPage.<PostResponse>builder()
                .items(items)
                .nextCursor(hasMore ? String.valueOf(offset + pageSize) : null)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    /** Replies to a post, oldest first (conversation order). */
    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getReplies(UUID viewerId, UUID postId, String cursorStr, int limit) {
        if (!postRepository.existsById(postId)) {
            throw BoondiException.postNotFound(postId.toString());
        }
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);
        List<Post> posts = postRepository.findReplies(postId, cursor, PageRequest.of(0, pageSize + 1));
        return buildPage(posts, pageSize, viewerId);
    }

    /** The authenticated user's bookmarked posts, most recently bookmarked first (E6-14). */
    @Transactional(readOnly = true)
    public CursorPage<PostResponse> getBookmarkedTimeline(UUID userId, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);
        List<Object[]> rows = postRepository.findBookmarkedPosts(userId, cursor, PageRequest.of(0, pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<Object[]> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<PostResponse> items = page.stream()
                .map(row -> postMapper.toResponse((Post) row[0]))
                .toList();
        postViewerStateService.enrich(userId, items);

        String nextCursor = (hasMore && !page.isEmpty())
                ? ((OffsetDateTime) page.get(page.size() - 1)[1]).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<PostResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private int clampLimit(int requested) {
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    private OffsetDateTime parseCursor(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return null;
        try {
            return OffsetDateTime.parse(cursorStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Invalid cursor value '{}' — treating as first page", cursorStr);
            return null;
        }
    }

    private int parseOffset(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return 0;
        try {
            return Math.min(Math.max(Integer.parseInt(cursorStr), 0), TRENDING_MAX_OFFSET);
        } catch (NumberFormatException e) {
            log.warn("Invalid trending cursor '{}' — treating as first page", cursorStr);
            return 0;
        }
    }

    private CursorPage<PostResponse> buildPage(List<Post> raw, int pageSize, UUID viewerId) {
        boolean hasMore = raw.size() > pageSize;
        List<Post> page = hasMore ? raw.subList(0, pageSize) : raw;

        List<PostResponse> items = page.stream().map(postMapper::toResponse).toList();
        postViewerStateService.enrich(viewerId, items);

        String nextCursor = (hasMore && !page.isEmpty())
                ? page.get(page.size() - 1).getCreatedAt()
                      .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<PostResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }
}
