package com.boondi.presentation.controller;

import com.boondi.application.dto.request.CreatePostRequest;
import com.boondi.application.dto.request.UpdatePostRequest;
import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.dto.response.UploadResponse;
import com.boondi.application.service.InteractionService;
import com.boondi.application.service.PostService;
import com.boondi.application.service.TimelineService;
import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.security.CustomUserDetailsService.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post creation, editing, deletion, retrieval, and interactions")
public class PostController {

    private final PostService postService;
    private final InteractionService interactionService;
    private final TimelineService timelineService;

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a post. Set parentPostId to reply, quotedPostId to quote another post.")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse post = postService.createPost(userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(post, "Post created successfully"));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by ID (public; viewer flags filled when authenticated)")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(postId, viewerId)));
    }

    @GetMapping("/{postId}/replies")
    @Operation(summary = "Get replies to a post, oldest first (public), cursor-paginated")
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> getReplies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId,
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getReplies(viewerId, postId, cursor, limit)));
    }

    @PutMapping("/{postId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Edit a post (author only, within 30 minutes of creation)")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request) {
        PostResponse post = postService.updatePost(postId, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(post, "Post updated successfully"));
    }

    @DeleteMapping("/{postId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete a post (author only, soft delete)")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        postService.deletePost(postId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Upload an image for a post (JPEG/PNG/WebP, max 5MB). Returns the image URL to include in createPost.")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadPostImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UploadResponse result = postService.uploadPostImage(userDetails.getUser().getId(), file);
        return ResponseEntity.ok(ApiResponse.success(result, "Image uploaded successfully"));
    }

    // ─── Interactions (E6-01, E6-02, E6-04) ─────────────────────────────────────

    @PostMapping("/{postId}/like")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse<PostResponse>> like(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.like(userDetails.getUser().getId(), postId), "Post liked"));
    }

    @DeleteMapping("/{postId}/like")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Remove your like from a post")
    public ResponseEntity<ApiResponse<PostResponse>> unlike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.unlike(userDetails.getUser().getId(), postId), "Like removed"));
    }

    @PostMapping("/{postId}/repost")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Repost a post")
    public ResponseEntity<ApiResponse<PostResponse>> repost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.repost(userDetails.getUser().getId(), postId), "Post reposted"));
    }

    @DeleteMapping("/{postId}/repost")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Remove your repost")
    public ResponseEntity<ApiResponse<PostResponse>> unrepost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.unrepost(userDetails.getUser().getId(), postId), "Repost removed"));
    }

    @PostMapping("/{postId}/bookmark")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Bookmark a post")
    public ResponseEntity<ApiResponse<PostResponse>> bookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.bookmark(userDetails.getUser().getId(), postId), "Post bookmarked"));
    }

    @DeleteMapping("/{postId}/bookmark")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Remove your bookmark")
    public ResponseEntity<ApiResponse<PostResponse>> unbookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.unbookmark(userDetails.getUser().getId(), postId), "Bookmark removed"));
    }
}
