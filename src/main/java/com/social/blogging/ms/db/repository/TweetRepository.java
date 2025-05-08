package com.social.blogging.ms.db.repository;

import com.social.blogging.ms.db.model.Tweets;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Repository
public class TweetRepository extends BaseRepository<Tweets> {

    private final DynamoDbTable<Tweets> tweetsTable;

    public TweetRepository(DynamoDbTable<Tweets> tweetsTable) {
        super(tweetsTable);
        this.tweetsTable = tweetsTable;
    }

    public Tweets save(Tweets tweet) {
            PutItemEnhancedRequest<Tweets> request = PutItemEnhancedRequest.builder(Tweets.class)
                    .item(tweet)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(user_id) AND attribute_not_exists(created_at)")
                            .build())
                    .build();

            tweetsTable.putItem(request);
            return tweet;
    }

}
