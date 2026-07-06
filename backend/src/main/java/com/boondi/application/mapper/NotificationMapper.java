package com.boondi.application.mapper;

import com.boondi.application.dto.response.NotificationResponse;
import com.boondi.domain.entity.Notification;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationMapper {

    private static final int PREVIEW_LENGTH = 80;

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .actor(toActorInfo(notification.getActor()))
                .postId(postId(notification))
                .postContentPreview(postPreview(notification))
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationResponse.ActorInfo toActorInfo(User user) {
        return NotificationResponse.ActorInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    // The related post may have been soft-deleted, which makes the lazy proxy
    // throw EntityNotFoundException under @SQLRestriction — fall back to null.
    private UUID postId(Notification notification) {
        try {
            Post post = notification.getPost();
            return post != null ? post.getId() : null;
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private String postPreview(Notification notification) {
        try {
            Post post = notification.getPost();
            if (post == null) return null;
            String content = post.getContent();
            return content.length() > PREVIEW_LENGTH
                    ? content.substring(0, PREVIEW_LENGTH) + "…"
                    : content;
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
