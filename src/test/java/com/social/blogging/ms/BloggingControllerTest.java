package com.social.blogging.ms;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.db.model.Tweets;
import com.social.blogging.ms.db.repository.FollowerRepository;
import com.social.blogging.ms.db.repository.TimeLineRepository;
import com.social.blogging.ms.db.repository.TweetRepository;
import com.social.blogging.ms.web.model.FollowerRequest;
import com.social.blogging.ms.web.model.TweetRequest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Testcontainers
public class BloggingControllerTest {

    static final String QUEUE_NAME = "blogging-timeline-queue";

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private TimeLineRepository timeLineRepository;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @LocalServerPort
    protected int port;

    private String queueUrl;

    @Container
    static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.3"))
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.region.static", () -> localStackContainer.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> localStackContainer.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> localStackContainer.getSecretKey());
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        registry.add("spring.cloud.aws.endpoint", () -> localStackContainer.getEndpoint());
        registry.add("aws.endpoint-url", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
        registry.add("blogger-service.queue", () -> QUEUE_NAME);
    }


    @BeforeEach
    public void setUp() throws Exception {

        try {
            cleanFollowsTable();
            cleanTweetsTable();

            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            queueUrl = sqsAsyncClient.getQueueUrl(getQueueUrlRequest).get().queueUrl();
        } catch (Exception e) {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            queueUrl = sqsAsyncClient.createQueue(createQueueRequest).get().queueUrl();
        }

    }

    @Test
    void test_saved_tweet_and_publish_ok() throws ExecutionException, InterruptedException {
        TweetRequest tweetRequest = new TweetRequest("test_user", "test tweet");

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/tweets")
                .setContentType(ContentType.JSON)
                .setBody(tweetRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value());

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(5)
                .build();

        ReceiveMessageResponse receiveMessageResponse = sqsAsyncClient.receiveMessage(receiveMessageRequest).get();

        Assertions.assertFalse(receiveMessageResponse.messages().isEmpty(), "we should have at least one message in this queue");

    }

    @Test
    public void test_saved_tweet_with_conflict() {
        saveTweetFakeData();
        TweetRequest tweetRequest = new TweetRequest("test_user_int_test", "test tweet", "1746657430905");

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/tweets")
                .setContentType(ContentType.JSON)
                .setBody(tweetRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", equalTo("we already have information for this user"));

    }

    @Test
    public void test_save_followers_ok() {
        FollowerRequest followerRequest = new FollowerRequest("test_user_1", "test tweet");

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/followers")
                .setContentType(ContentType.JSON)
                .setBody(followerRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value());

    }

    @Test
    @DisplayName("User already have a follower")
    public void test_save_followers_conflict() {

        saveFakeFollowData();
        FollowerRequest followerRequest = new FollowerRequest("test_user_test", "user_follower_test");

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/followers")
                .setContentType(ContentType.JSON)
                .setBody(followerRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", equalTo("we already have information for this user"));

    }

    @Test
    public void test_get_time_line_ok() {

        saveFakeTimeLineData();

        RequestSpecification timeLineSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/timeline/user_follower")
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(timeLineSpecification)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("timelines", notNullValue())
                .body("timelines.size()", equalTo(1))
                .body("timelines[0].content", equalTo("content message"))
                .body("timelines[0].author", equalTo("test_user"))
                .body("timelines[0].createdAt", notNullValue());


    }

    @ParameterizedTest
    @DisplayName("When tweet request has invalid data then return BadRequest")
    @MethodSource("com.social.blogging.ms.UtilTest#provideTweetRequestInvalidData")
    void test_whenTweetRequestHasInvalidData_thenBadRequest(String username, String content) {
        TweetRequest tweetRequest = new TweetRequest(username, content);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/tweets")
                .setContentType(ContentType.JSON)
                .setBody(tweetRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("errors", notNullValue())
                .body("timestamp", notNullValue());
    }

    @ParameterizedTest
    @DisplayName("When follower request has invalid data then return BadRequest")
    @MethodSource("com.social.blogging.ms.UtilTest#provideFollowerRequestInvalidData")
    void test_whenFollowerRequestHasInvalidData_thenBadRequest(String author, String follower) {
        FollowerRequest followerRequest = new FollowerRequest(author, follower);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/followers")
                .setContentType(ContentType.JSON)
                .setBody(followerRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("errors", notNullValue())
                .body("timestamp", notNullValue());
    }


    @Test
    public void test_when_weet_request_withTooLongContent_thenBadRequest() {
        String tooLongContent = "a".repeat(281); // Asumiendo un límite de 280 caracteres
        TweetRequest tweetRequest = new TweetRequest("testuser", tooLongContent);

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + port)
                .setBasePath("/v1/tweets")
                .setContentType(ContentType.JSON)
                .setBody(tweetRequest)
                .log(LogDetail.ALL)
                .build();

        given()
                .spec(requestSpecification)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("errors", notNullValue())
                .body("timestamp", notNullValue());
    }

    private void saveFakeTimeLineData() {
        timeLineRepository.saveTimeline(Timeline.builder()
                .authorId("test_user")
                .followedId("user_follower")
                .content("content message")
                .timelineId("test#1746579206103#af8fa2df-a723-444d-93bf-a0ac45b986cc")
                .tweetId("2a4b026b-2aa7-4a1e-8875-8e908dff9dff")
                .createdAt(System.currentTimeMillis())
                .build());
    }

    private void saveFakeFollowData() {
        followerRepository.save(Follow.builder()
                .followedAt(System.currentTimeMillis())
                .followeeId("user_follower_test")
                .followerId("test_user_test")
                .build());
    }

    private void cleanFollowsTable() {
        followerRepository.truncateTable();
    }

    private void cleanTweetsTable() {
        tweetRepository.truncateTable();
    }

    private void saveTweetFakeData() {
        tweetRepository.save(Tweets.builder()
                .tweetId("2a4b026b-2aa7-4a1e-8875-8e908dff9dff")
                .content("test tweet")
                .createdAt("1746657430905")
                .userId("test_user_int_test")
                .build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        sqsAsyncClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build()).get();
    }
}
