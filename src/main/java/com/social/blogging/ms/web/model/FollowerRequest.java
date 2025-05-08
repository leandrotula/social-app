package com.social.blogging.ms.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record FollowerRequest(@JsonProperty("author_username")
                              @NotBlank(message = "author username cannot be blank")
                              String authorUsername,
                              @NotBlank(message = "followed username cannot be blank")
                              String followed) {
}
