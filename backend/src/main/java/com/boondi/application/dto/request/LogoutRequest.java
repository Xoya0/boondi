package com.boondi.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Logout request — refresh token is optional but recommended for full revocation")
public class LogoutRequest {

    @Schema(description = "The refresh token to revoke", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
