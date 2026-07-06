package com.boondi.application.dto.response;

import com.boondi.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private NotificationType type;
    private ActorInfo actor;

    // Null for FOLLOW notifications
    private UUID postId;
    private String postContentPreview;

    private boolean read;
    private OffsetDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        private UUID id;
        private String username;
        private String displayName;
        private String profilePictureUrl;
    }
}
