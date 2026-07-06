package com.boondi.presentation.controller;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.HashtagResponse;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.service.SearchService;
import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.security.CustomUserDetailsService.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search users, posts, and hashtags (public)")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/users")
    @Operation(summary = "Search users by username or display name (public)")
    public ResponseEntity<ApiResponse<CursorPage<UserResponse>>> searchUsers(
            @RequestParam String q,
            @Parameter(description = "Pagination cursor (numeric offset from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchUsers(q, cursor, limit)));
    }

    @GetMapping("/posts")
    @Operation(summary = "Full-text search over post content (public; viewer flags filled when authenticated)")
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> searchPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String q,
            @Parameter(description = "Pagination cursor (numeric offset from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(searchService.searchPosts(viewerId, q, cursor, limit)));
    }

    @GetMapping("/hashtags")
    @Operation(summary = "Search hashtags by prefix (public)")
    public ResponseEntity<ApiResponse<CursorPage<HashtagResponse>>> searchHashtags(
            @RequestParam String q,
            @Parameter(description = "Pagination cursor (numeric offset from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchHashtags(q, cursor, limit)));
    }
}
