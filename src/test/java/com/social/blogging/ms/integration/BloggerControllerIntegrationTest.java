package com.social.blogging.ms.integration;

import com.social.blogging.ms.web.model.FollowerRequest;
import com.social.blogging.ms.web.model.TweetRequest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;

public class BloggerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    private String queueUrl;

    @Before
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
    public void test_saved_tweet_and_publish_ok() throws ExecutionException, InterruptedException {
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

        assertFalse("we should have at least one message in this queue", receiveMessageResponse.messages().isEmpty());

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

    @Test
    public void whenTweetRequestWithNullUsername_thenBadRequest() {
        TweetRequest tweetRequest = new TweetRequest(null, "test tweet");

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

    @Test
    public void test_when_tweetRequest_withEmpty_content_thenBadRequest() {
        TweetRequest tweetRequest = new TweetRequest("testuser", "");

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

    @Test
    public void test_when_follower_request_with_invalid_data_thenBadRequest() {
        FollowerRequest followerRequest = new FollowerRequest("", "");

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
                .body("errors", hasSize(greaterThan(0)))
                .body("timestamp", notNullValue());
    }

    @Test
    public void whenMultipleValidationErrors_thenAllErrorsReturned() {
        TweetRequest tweetRequest = new TweetRequest("", ""); // Múltiples campos inválidos

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
                .body("errors", hasSize(greaterThan(1)))
                .body("timestamp", notNullValue());
    }


    @After
    public void tearDown() throws Exception {
        sqsAsyncClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build()).get();
    }

}
