package com.boondi.application.dto.response;

import com.boondi.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile data")
public class UserResponse {

    @Schema(description = "Unique user identifier")
    private UUID id;

    @Schema(description = "Unique username", example = "john_doe")
    private String username;

    @Schema(description = "User email address", example = "john@example.com")
    private String email;

    @Schema(description = "Display name", example = "John Doe")
    private String displayName;

    @Schema(description = "User bio / about text")
    private String bio;

    @Schema(description = "URL of the user's profile picture")
    private String profilePictureUrl;

    @Schema(description = "URL of the user's banner image")
    private String bannerImageUrl;

    @Schema(description = "User role", example = "USER")
    private UserRole role;

    @Schema(description = "Whether the user's email has been verified")
    private boolean emailVerified;

    @Schema(description = "Number of followers")
    private int followerCount;

    @Schema(description = "Number of accounts this user follows")
    private int followingCount;

    @Schema(description = "Total number of posts")
    private int postCount;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Whether the authenticated viewer follows this user. Null for anonymous requests or own profile.")
    private Boolean followedByViewer;
}
