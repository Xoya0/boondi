package com.boondi.application.dto.response;

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
public class ReportResponse {

    private UUID id;
    private ReporterInfo reporter;

    // Exactly one of these is non-null.
    private ReportedUserInfo reportedUser;
    private ReportedPostInfo reportedPost;

    private String reason;
    private OffsetDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporterInfo {
        private UUID id;
        private String username;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedUserInfo {
        private UUID id;
        private String username;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedPostInfo {
        private UUID id;
        private String contentPreview;
        private String authorUsername;
    }
}
