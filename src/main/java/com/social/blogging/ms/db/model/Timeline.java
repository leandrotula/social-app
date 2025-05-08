package com.social.blogging.ms.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@Builder(builderClassName = "TimelineBuilder")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonDeserialize(builder = Timeline.TimelineBuilder.class)
public class Timeline {
    private String followedId;
    private Long createdAt;
    private String tweetId;
    private String content;
    private String authorId;
    private String timelineId;

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi-followed_id-index")
    @DynamoDbAttribute("followed_id")
    @JsonProperty("followed_id")
    public String getFollowedId() {
        return followedId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("timeline_id")
    @JsonProperty("timeline_id")
    public String getTimelineId() {
        return timelineId;
    }

    @DynamoDbAttribute("created_at")
    @JsonProperty("created_at")
    public Long getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("tweet_id")
    @JsonProperty("tweet_id")
    @DynamoDbPartitionKey
    public String getTweetId() {
        return tweetId;
    }

    @DynamoDbAttribute("content")
    public String getContent() {
        return content;
    }

    @DynamoDbAttribute("author_id")
    @JsonProperty("author_id")
    public String getAuthorId() {
        return authorId;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TimelineBuilder {
    }
}
