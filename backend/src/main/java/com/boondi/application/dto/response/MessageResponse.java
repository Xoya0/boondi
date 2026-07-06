package com.boondi.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple message response")
public class MessageResponse {

    @Schema(description = "Human-readable result message", example = "Email verified successfully")
    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
