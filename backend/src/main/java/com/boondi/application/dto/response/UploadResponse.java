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
@Schema(description = "File upload response")
public class UploadResponse {

    @Schema(description = "Public URL of the uploaded file",
            example = "http://localhost:9000/boondi-media/avatars/uuid/avatar.jpg")
    private String url;

    public static UploadResponse of(String url) {
        return new UploadResponse(url);
    }
}
