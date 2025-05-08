package com.social.blogging.ms.integration;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.db.model.Tweets;
import com.social.blogging.ms.db.repository.FollowerRepository;
import com.social.blogging.ms.db.repository.TimeLineRepository;
import com.social.blogging.ms.db.repository.TweetRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@RunWith(SpringRunner.class)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    private TimeLineRepository timeLineRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private FollowerRepository followerRepository;


    static LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.3"))
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

    static final String QUEUE_NAME = "blogging-timeline-queue";

    static {
        Startables.deepStart(localStackContainer).join();
    }

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint",
                () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
    }


    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
        dynamicPropertyRegistry.add("blogger-service.queue", () -> QUEUE_NAME);
        dynamicPropertyRegistry.add("spring.cloud.aws.region.static", () -> localStackContainer.getRegion());
        dynamicPropertyRegistry.add("spring.cloud.aws.credentials.access-key", () -> localStackContainer.getAccessKey());
        dynamicPropertyRegistry.add("spring.cloud.aws.credentials.secret-key", () -> localStackContainer.getSecretKey());
        dynamicPropertyRegistry.add("spring.cloud.aws.sqs.endpoint",
                () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        dynamicPropertyRegistry.add("spring.cloud.aws.dynamodb.endpoint",
                () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        dynamicPropertyRegistry.add("spring.cloud.aws.endpoint", () -> localStackContainer.getEndpoint());
        dynamicPropertyRegistry.add("aws.endpoint-url",
                () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());

    }

    protected void saveFakeTimeLineData() {

        timeLineRepository.saveTimeline(Timeline.builder()
                        .authorId("test_user")
                        .followedId("user_follower")
                        .content("content message")
                        .timelineId("test#1746579206103#af8fa2df-a723-444d-93bf-a0ac45b986cc")
                        .tweetId("2a4b026b-2aa7-4a1e-8875-8e908dff9dff")
                .createdAt(System.currentTimeMillis())
                .build());
    }

    protected void saveFakeFollowData() {

        followerRepository.save(Follow.builder()
                        .followedAt(System.currentTimeMillis())
                        .followeeId("user_follower_test")
                        .followerId("test_user_test")
                .build());
    }

    protected void cleanFollowsTable() {
        followerRepository.truncateTable();
    }

    protected void cleanTweetsTable() {
        tweetRepository.truncateTable();
    }

    protected void saveTweetFakeData() {
        tweetRepository.save(Tweets.builder()
                .tweetId("2a4b026b-2aa7-4a1e-8875-8e908dff9dff")
                .content("test tweet")
                .createdAt("1746657430905")
                .userId("test_user_int_test")
                .build());
    }
}
