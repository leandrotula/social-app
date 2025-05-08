package com.social.blogging.ms.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSender {

    private final SqsTemplate sqsTemplate;

    @Value("${spring.cloud.aws.sqs.endpoint}")
    private String sqsEndpoint;

    @Value("${blogger-service.queue}")
    private String queueName;

    public void publish(Object payload) {
        try {
            sqsTemplate.send(to -> {
                        try {
                            to
                                    .queue(queueName)
                                    .payload(new ObjectMapper().writeValueAsString(payload));
                        } catch (Exception e) {
                            log.error("Error sending payload: {}", e.getMessage(), e);
                        }
                    }
            );

            log.info("Message sent to SQS. Queue: {}, Region: {}", queueName, sqsEndpoint);
        } catch (Exception e) {
            log.error("Failed to publish message. Queue: {}, Region: {}, Error: {}",
                    queueName, sqsEndpoint, e.getMessage(), e);
            throw new RuntimeException("Error publishing message to SQS", e);
        }
    }
}
