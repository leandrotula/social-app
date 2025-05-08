package com.social.blogging.ms.db.repository;

import com.social.blogging.ms.db.model.Timeline;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class TimeLineRepository {

    private static final String GSI_FOLLOWED_ID_INDEX = "gsi-followed_id-index";

    private final DynamoDbTable<Timeline> timelinesTable;

    public List<Timeline> getTimelineByFollowedId(String followeeId) {
        DynamoDbIndex<Timeline> index = timelinesTable.index(GSI_FOLLOWED_ID_INDEX);

        List<Timeline> timelines = new ArrayList<>();

        index.query(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue(followeeId)
                        .build()))
                .forEach(page -> timelines.addAll(page.items()));

        return timelines;
    }

    public void saveTimeline(Timeline timeline) {
        timelinesTable.putItem(timeline);
    }

}
