package com.boondi.presentation.controller;

import com.boondi.application.dto.request.UpdateProfileRequest;
import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.dto.response.UploadResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.service.FollowService;
import com.boondi.application.service.TimelineService;
import com.boondi.application.service.UserService;
import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.security.CustomUserDetailsService.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;
    private final TimelineService timelineService;
    private final FollowService followService;

    @GetMapping("/{username}")
    @Operation(summary = "Get a user profile by username (public; followedByViewer filled when authenticated)")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(username, viewerId)));
    }

    @GetMapping("/{username}/posts")
    @Operation(summary = "Get posts by a specific user (public), cursor-paginated")
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> getUserPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username,
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getUserTimeline(viewerId, username, cursor, limit)));
    }

    // ─── Follow (E6-05 → E6-07) ─────────────────────────────────────────────────

    @PostMapping("/{username}/follow")
    @Operation(summary = "Follow a user. Returns the target's updated profile.")
    public ResponseEntity<ApiResponse<UserResponse>> follow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.follow(userDetails.getUser().getId(), username), "User followed"));
    }

    @DeleteMapping("/{username}/follow")
    @Operation(summary = "Unfollow a user. Returns the target's updated profile.")
    public ResponseEntity<ApiResponse<UserResponse>> unfollow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.unfollow(userDetails.getUser().getId(), username), "User unfollowed"));
    }

    @GetMapping("/{username}/followers")
    @Operation(summary = "List a user's followers, newest first (public), cursor-paginated")
    public ResponseEntity<ApiResponse<CursorPage<UserResponse>>> getFollowers(
            @PathVariable String username,
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getFollowers(username, cursor, limit)));
    }

    @GetMapping("/{username}/following")
    @Operation(summary = "List users a user follows, newest first (public), cursor-paginated")
    public ResponseEntity<ApiResponse<CursorPage<UserResponse>>> getFollowing(
            @PathVariable String username,
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getFollowing(username, cursor, limit)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse updated = userService.updateProfile(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile avatar (JPEG/PNG/WebP, max 5MB)")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UploadResponse result = userService.updateAvatar(userDetails.getUser().getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, "Avatar uploaded successfully"));
    }

    @PostMapping(value = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile banner (JPEG/PNG/WebP, max 10MB)")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadBanner(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UploadResponse result = userService.updateBanner(userDetails.getUser().getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, "Banner uploaded successfully"));
    }
}
