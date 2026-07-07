package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Admin moderation actions (Epic 9). All endpoints calling into this service are gated by
 * {@code @PreAuthorize("hasRole('ADMIN')")} at the controller (E9-01) — Spring Security's
 * role check reads the `ROLE_ADMIN` authority already granted via {@code User.role}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int MAX_LIMIT = 50;

    private final UserRepository userRepository;
    private final PostService postService;
    private final UserMapper userMapper;

    /** Paginated user list backing the admin panel's user table (E9-06). */
    @Transactional(readOnly = true)
    public CursorPage<UserResponse> getUsers(String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);

        List<User> raw = userRepository.findAllForAdmin(cursor, PageRequest.of(0, pageSize + 1));
        boolean hasMore = raw.size() > pageSize;
        List<User> page = hasMore ? raw.subList(0, pageSize) : raw;

        List<UserResponse> items = page.stream().map(userMapper::toResponse).toList();
        String nextCursor = (hasMore && !page.isEmpty())
                ? page.get(page.size() - 1).getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<UserResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    @Transactional
    public UserResponse suspendUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BoondiException.userNotFound(userId.toString()));
        user.setSuspended(true);
        User saved = userRepository.save(user);
        log.info("User suspended by admin: userId={}", userId);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse unsuspendUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BoondiException.userNotFound(userId.toString()));
        user.setSuspended(false);
        User saved = userRepository.save(user);
        log.info("User unsuspended by admin: userId={}", userId);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void deletePost(UUID postId) {
        postService.adminDeletePost(postId);
    }

    private int clampLimit(int requested) {
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    // User.createdAt is OffsetDateTime but the admin listing cursor round-trips through the
    // same ISO-8601 string format the rest of the app uses for time-based cursors.
    private OffsetDateTime parseCursor(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return null;
        try {
            return OffsetDateTime.parse(cursorStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Invalid admin user cursor '{}' — treating as first page", cursorStr);
            return null;
        }
    }
}
