package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.Follow;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.FollowRepository;
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
 * Follow / Unfollow + follower/following lists (E6-05 → E6-07).
 * Inserts/deletes rows in `follows` and keeps follower_count / following_count
 * on User in sync, per the counters-on-entity design decision.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private static final int MAX_LIMIT = 50;

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TimelineCacheService timelineCacheService;

    @Transactional
    public UserResponse follow(UUID followerId, String username) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> BoondiException.userNotFound(username));
        if (target.getId().equals(followerId)) {
            throw BoondiException.cannotFollowSelf();
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, target.getId())) {
            throw BoondiException.alreadyFollowing();
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> BoondiException.userNotFound(followerId.toString()));

        followRepository.save(Follow.builder()
                .followerId(followerId)
                .followeeId(target.getId())
                .build());

        target.setFollowerCount(target.getFollowerCount() + 1);
        follower.setFollowingCount(follower.getFollowingCount() + 1);
        userRepository.save(target);
        userRepository.save(follower);

        // Follower's home timeline now includes the target's posts
        timelineCacheService.evictHome(followerId);

        log.info("User followed: followerId={}, followee={}", followerId, username);
        UserResponse response = userMapper.toResponse(target);
        response.setFollowedByViewer(true);
        return response;
    }

    @Transactional
    public UserResponse unfollow(UUID followerId, String username) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> BoondiException.userNotFound(username));
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId, target.getId())) {
            throw BoondiException.notFollowing();
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> BoondiException.userNotFound(followerId.toString()));

        followRepository.deleteByFollowerIdAndFolloweeId(followerId, target.getId());

        target.setFollowerCount(Math.max(0, target.getFollowerCount() - 1));
        follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
        userRepository.save(target);
        userRepository.save(follower);

        timelineCacheService.evictHome(followerId);

        log.info("User unfollowed: followerId={}, followee={}", followerId, username);
        UserResponse response = userMapper.toResponse(target);
        response.setFollowedByViewer(false);
        return response;
    }

    @Transactional(readOnly = true)
    public CursorPage<UserResponse> getFollowers(String username, String cursorStr, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BoondiException.userNotFound(username));
        int pageSize = clampLimit(limit);
        List<Object[]> rows = followRepository.findFollowers(
                user.getId(), parseCursor(cursorStr), PageRequest.of(0, pageSize + 1));
        return buildUserPage(rows, pageSize);
    }

    @Transactional(readOnly = true)
    public CursorPage<UserResponse> getFollowing(String username, String cursorStr, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BoondiException.userNotFound(username));
        int pageSize = clampLimit(limit);
        List<Object[]> rows = followRepository.findFollowing(
                user.getId(), parseCursor(cursorStr), PageRequest.of(0, pageSize + 1));
        return buildUserPage(rows, pageSize);
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
            log.warn("Invalid follow cursor '{}' — treating as first page", cursorStr);
            return null;
        }
    }

    // Rows are [User, follow.createdAt] — the follow timestamp is the cursor
    private CursorPage<UserResponse> buildUserPage(List<Object[]> raw, int pageSize) {
        boolean hasMore = raw.size() > pageSize;
        List<Object[]> page = hasMore ? raw.subList(0, pageSize) : raw;

        List<UserResponse> items = page.stream()
                .map(row -> userMapper.toResponse((User) row[0]))
                .toList();

        String nextCursor = (hasMore && !page.isEmpty())
                ? ((OffsetDateTime) page.get(page.size() - 1)[1])
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<UserResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }
}
