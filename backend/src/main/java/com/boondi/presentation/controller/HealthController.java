package com.boondi.presentation.controller;

import com.boondi.infrastructure.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Service health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns service status and version information")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("service", "boondi-backend");
        status.put("version", "0.1.0");
        return ResponseEntity.ok(ApiResponse.success(status, "Service is healthy"));
    }
}
