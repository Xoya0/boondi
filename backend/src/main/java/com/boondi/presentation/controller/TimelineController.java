package com.boondi.presentation.controller;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.service.TimelineService;
import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.security.CustomUserDetailsService.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/timelines")
@RequiredArgsConstructor
@Tag(name = "Timelines", description = "Post feed APIs — latest (public), trending (public), home (personalized, auth required)")
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/latest")
    @Operation(
        summary = "Latest timeline — all posts, reverse-chronological (public)",
        description = "Pass ?cursor=<ISO-8601> from the previous response's nextCursor to paginate."
    )
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> getLatest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Pagination cursor (ISO-8601 timestamp from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getLatestTimeline(viewerId, cursor, limit)));
    }

    @GetMapping("/trending")
    @Operation(
        summary = "Trending timeline — last 24h scored by likes + 2×reposts (public)",
        description = "The cursor for this endpoint is a numeric offset from the previous response's nextCursor."
    )
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> getTrending(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Pagination cursor (numeric offset from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        UUID viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getTrendingTimeline(viewerId, cursor, limit)));
    }

    @GetMapping("/home")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Home timeline — posts from users you follow (auth required)",
        description = "First page is served from Redis (5-minute TTL) when available."
    )
    public ResponseEntity<ApiResponse<CursorPage<PostResponse>>> getHome(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Pagination cursor (ISO-8601 timestamp from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getHomeTimeline(userDetails.getUser().getId(), cursor, limit)));
    }
}
