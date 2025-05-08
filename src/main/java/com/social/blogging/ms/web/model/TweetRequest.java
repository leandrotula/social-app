package com.social.blogging.ms.web.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TweetRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Content cannot be blank")
        @Size(max = 280, message = "Tweet cannot exceed 280 characters")
        String content,

        @Nullable
        String createdAt
) {
    public TweetRequest(String username, String content) {
        this(username, content, null);
    }
}
