package com.social.blogging.ms.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@Builder(builderClassName = "FollowBuilder")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonDeserialize(builder = Follow.FollowBuilder.class)
public class Follow {
    private String followerId;
    private String followeeId;
    private Long followedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("follower_id")
    @JsonProperty("follower_id")
    public String getFollowerId() {
        return followerId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("followed_id")
    @JsonProperty("followed_id")
    public String getFolloweeId() {
        return followeeId;
    }

    @DynamoDbAttribute("followed_id")
    public String getFolloweeIdIndex() {
        return followeeId;
    }

    @DynamoDbAttribute("follower_id")
    public String getFollowerIdIndex() {
        return followerId;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class FollowBuilder {
    }
}

