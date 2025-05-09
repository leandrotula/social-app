package com.social.blogging.ms.configuration;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.db.model.Tweets;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Component
public class DynamoDBInitializer {

    private final DynamoDbTable<Timeline> timelinesTable;
    private final DynamoDbTable<Follow> followsTable;
    private final DynamoDbTable<Tweets> tweetsTable;

    public DynamoDBInitializer(DynamoDbTable<Timeline> timelinesTable, DynamoDbTable<Follow> followsTable, DynamoDbTable<Tweets> tweetsTable) {
        this.timelinesTable = timelinesTable;
        this.followsTable = followsTable;
        this.tweetsTable = tweetsTable;
    }

    @PostConstruct
    public void createTablesIfNotExist() {
        createTableIfNotExists(timelinesTable);
        createTableIfNotExists(followsTable);
        createTableIfNotExists(tweetsTable);
    }

    private void createTableIfNotExists(DynamoDbTable<?> table) {
        try {
            table.describeTable();
        } catch (ResourceNotFoundException e) {
            table.createTable();
        }
    }
}

