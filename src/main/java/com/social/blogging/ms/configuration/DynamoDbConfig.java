package com.social.blogging.ms.configuration;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.db.model.Tweets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(@Value("${AWS_ENDPOINT_URL:https://dynamodb.us-east-2.amazonaws.com}") String endpointUrl) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.US_EAST_2)
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<Timeline> timelinesTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("Timelines", TableSchema.fromBean(Timeline.class));
    }

    @Bean
    public DynamoDbTable<Follow> followsTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("Follows", TableSchema.fromBean(Follow.class));
    }

    @Bean
    public DynamoDbTable<Tweets> tweetsTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("Tweets", TableSchema.fromBean(Tweets.class));
    }

}

