package com.boondi.presentation.controller;

import com.boondi.application.dto.response.HashtagResponse;
import com.boondi.application.service.SearchService;
import com.boondi.infrastructure.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hashtags")
@RequiredArgsConstructor
@Tag(name = "Hashtags", description = "Trending hashtags (public)")
public class HashtagController {

    private final SearchService searchService;

    @GetMapping("/trending")
    @Operation(summary = "Top 10 hashtags by usage in the last 24h (public, Redis-cached)")
    public ResponseEntity<ApiResponse<List<HashtagResponse>>> getTrending() {
        return ResponseEntity.ok(ApiResponse.success(searchService.getTrendingHashtags()));
    }
}
