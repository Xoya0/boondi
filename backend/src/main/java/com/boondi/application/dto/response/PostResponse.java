package com.boondi.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

// No-args constructor required: instances are round-tripped through Redis as JSON
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;
    private String content;
    private String imageUrl;
    private AuthorInfo author;
    private int likeCount;
    private int repostCount;
    private int replyCount;
    private int bookmarkCount;
    private UUID parentPostId;
    private QuotedPostInfo quotedPost;
    private boolean edited;
    private OffsetDateTime editedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Per-viewer interaction state — false for anonymous requests
    private boolean likedByViewer;
    private boolean repostedByViewer;
    private boolean bookmarkedByViewer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private UUID id;
        private String username;
        private String displayName;
        private String profilePictureUrl;
    }

    // Shallow embed of the quoted post (no nested quote chains)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotedPostInfo {
        private UUID id;
        private String content;
        private String imageUrl;
        private AuthorInfo author;
        private OffsetDateTime createdAt;
    }
}
