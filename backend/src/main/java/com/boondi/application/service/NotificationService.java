package com.boondi.application.service;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.NotificationResponse;
import com.boondi.application.mapper.NotificationMapper;
import com.boondi.domain.entity.Notification;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.User;
import com.boondi.domain.enums.NotificationType;
import com.boondi.domain.repository.NotificationRepository;
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
 * Fan-out-on-write notification creation (E7-01) + read APIs (E7-02 → E7-04).
 * Notify* methods are called from InteractionService, PostService, and FollowService
 * right after the triggering action succeeds. Self-notifications are always skipped
 * (e.g. liking your own post never creates a notification).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_LIMIT = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void notifyLike(UUID actorId, Post post) {
        create(NotificationType.LIKE, actorId, post.getAuthor().getId(), post);
    }

    @Transactional
    public void notifyRepost(UUID actorId, Post post) {
        create(NotificationType.REPOST, actorId, post.getAuthor().getId(), post);
    }

    @Transactional
    public void notifyReply(UUID actorId, Post parentPost) {
        create(NotificationType.REPLY, actorId, parentPost.getAuthor().getId(), parentPost);
    }

    @Transactional
    public void notifyFollow(UUID actorId, UUID recipientId) {
        create(NotificationType.FOLLOW, actorId, recipientId, null);
    }

    private void create(NotificationType type, UUID actorId, UUID recipientId, Post post) {
        if (actorId.equals(recipientId)) return; // no self-notifications

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> BoondiException.userNotFound(actorId.toString()));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> BoondiException.userNotFound(recipientId.toString()));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .post(post)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created: type={}, actorId={}, recipientId={}", type, actorId, recipientId);
    }

    @Transactional(readOnly = true)
    public CursorPage<NotificationResponse> getNotifications(UUID userId, String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);
        List<Notification> raw = notificationRepository.findByRecipient(
                userId, cursor, PageRequest.of(0, pageSize + 1));

        boolean hasMore = raw.size() > pageSize;
        List<Notification> page = hasMore ? raw.subList(0, pageSize) : raw;

        List<NotificationResponse> items = page.stream().map(notificationMapper::toResponse).toList();

        String nextCursor = (hasMore && !page.isEmpty())
                ? page.get(page.size() - 1).getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<NotificationResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> BoondiException.notificationNotFound(notificationId.toString()));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw BoondiException.notificationAccessDenied();
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
        log.info("All notifications marked read for userId={}", userId);
    }

    private int clampLimit(int requested) {
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    private OffsetDateTime parseCursor(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return null;
        try {
            return OffsetDateTime.parse(cursorStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Invalid notification cursor '{}' — treating as first page", cursorStr);
            return null;
        }
    }
}
