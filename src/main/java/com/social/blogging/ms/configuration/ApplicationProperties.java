package com.social.blogging.ms.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blogger-service")
public record ApplicationProperties(String queue, String bucket) {
}
