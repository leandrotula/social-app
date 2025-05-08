package com.social.blogging.ms.db.repository;

import com.social.blogging.ms.db.model.Follow;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@Repository
public class FollowerRepository extends BaseRepository<Follow> {

    private final DynamoDbTable<Follow> followsTable;

    public FollowerRepository(DynamoDbTable<Follow> followsTable) {
        super(followsTable);
        this.followsTable = followsTable;
    }

    public Follow save(Follow follow) {
        Expression conditionExpression = Expression.builder()
                .expression("attribute_not_exists(follower_id) AND attribute_not_exists(followed_id)")
                .build();

        PutItemEnhancedRequest<Follow> request = PutItemEnhancedRequest.builder(Follow.class)
                .item(follow)
                .conditionExpression(conditionExpression)
                .build();

        followsTable.putItem(request);
        return follow;
    }

    public void truncateTable() {
        followsTable.scan(ScanEnhancedRequest.builder().build())
                .items()
                .forEach(followsTable::deleteItem);
    }

}
