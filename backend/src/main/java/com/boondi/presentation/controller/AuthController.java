package com.boondi.presentation.controller;

import com.boondi.application.dto.request.*;
import com.boondi.application.dto.response.AuthResponse;
import com.boondi.application.dto.response.MessageResponse;
import com.boondi.application.service.AuthService;
import com.boondi.application.service.EmailVerificationService;
import com.boondi.application.service.PasswordResetService;
import com.boondi.application.service.TokenService;
import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.security.CustomUserDetailsService.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, token management, and password reset")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request), "User registered successfully. Please verify your email."));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tokenService.refresh(request), "Token refreshed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MessageResponse>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest) {

        String rawToken = extractBearerToken(httpRequest);
        tokenService.logout(userDetails.getUser().getId(), rawToken, request);
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.of("Logged out successfully")));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address using token from email link")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.of("Email verified successfully")));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MessageResponse>> resendVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        emailVerificationService.resendVerification(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.of("Verification email sent")));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                MessageResponse.of("If that email is registered, a password reset link has been sent.")));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token from email link")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.of("Password reset successfully")));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
