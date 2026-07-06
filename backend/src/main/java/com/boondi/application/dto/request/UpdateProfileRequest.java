package com.boondi.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update profile request — all fields are optional; only provided fields are updated")
public class UpdateProfileRequest {

    @Size(max = 100, message = "Display name must be at most 100 characters")
    @Schema(description = "Display name", example = "Jane Doe")
    private String displayName;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    @Schema(description = "User biography", example = "Coffee enthusiast. Developer. Dog lover.")
    private String bio;

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$",
             message = "Username must be 3–50 characters and contain only letters, numbers, and underscores")
    @Schema(description = "Username (3–50 chars, alphanumeric and underscores only)", example = "jane_doe")
    private String username;
}
