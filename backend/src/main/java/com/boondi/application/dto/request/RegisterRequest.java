package com.boondi.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user registration")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,50}$",
            message = "Username must be 3-50 characters and contain only letters, numbers, and underscores"
    )
    @Schema(description = "Unique username (3-50 chars, alphanumeric and underscore only)", example = "john_doe")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "User email address", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Password (8-100 characters)", example = "securePassword123")
    private String password;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Schema(description = "Display name (optional, max 100 chars)", example = "John Doe")
    private String displayName;
}
