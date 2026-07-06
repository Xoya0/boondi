package com.boondi.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Content cannot exceed 500 characters")
    private String content;

    private String imageUrl;

    // When set, this post is a reply to the given post
    private UUID parentPostId;

    // When set, this post quotes the given post
    private UUID quotedPostId;
}
