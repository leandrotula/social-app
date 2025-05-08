package com.social.blogging.ms.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@Builder(builderClassName = "TweetsBuilder")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonDeserialize(builder = Tweets.TweetsBuilder.class)
public class Tweets {

    private String userId;
    private String createdAt;
    private String tweetId;
    private String content;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("user_id")
    @JsonProperty("user_id")
    public String getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("created_at")
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class TweetsBuilder {
    }
}


