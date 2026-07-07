package com.boondi.presentation.controller;

import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.MessageResponse;
import com.boondi.application.dto.response.ReportResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.service.AdminService;
import com.boondi.application.service.ReportService;
import com.boondi.infrastructure.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin moderation endpoints (Epic 9). Every method requires the ADMIN role — enforced via
 * {@code @PreAuthorize}, which Spring Security evaluates against the `ROLE_ADMIN` authority
 * granted by {@link com.boondi.infrastructure.security.CustomUserDetailsService} (E9-01).
 * Non-admins get a 403 with {@code ACCESS_DENIED} from the global exception handler.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only moderation: user suspension, post removal, reports")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/users")
    @Operation(summary = "List users, newest first, cursor-paginated (admin only)")
    public ResponseEntity<ApiResponse<CursorPage<UserResponse>>> getUsers(
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(cursor, limit)));
    }

    @PutMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.suspendUser(userId), "User suspended"));
    }

    @PutMapping("/users/{userId}/unsuspend")
    @Operation(summary = "Unsuspend a user account")
    public ResponseEntity<ApiResponse<UserResponse>> unsuspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.unsuspendUser(userId), "User unsuspended"));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "Delete any post (moderation), bypassing author ownership checks")
    public ResponseEntity<ApiResponse<MessageResponse>> deletePost(@PathVariable UUID postId) {
        adminService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.of("Post deleted")));
    }

    @GetMapping("/reports")
    @Operation(summary = "List user-submitted reports, newest first, cursor-paginated")
    public ResponseEntity<ApiResponse<CursorPage<ReportResponse>>> getReports(
            @Parameter(description = "Pagination cursor (ISO-8601 from previous nextCursor)")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size (1–50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReports(cursor, limit)));
    }
}
