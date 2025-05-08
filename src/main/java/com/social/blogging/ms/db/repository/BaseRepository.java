package com.social.blogging.ms.db.repository;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@AllArgsConstructor
public class BaseRepository<T> {

    private final DynamoDbTable<T> table;

    public void truncateTable() {
        table.scan(ScanEnhancedRequest.builder().build())
                .items()
                .forEach(table::deleteItem);
    }

}
