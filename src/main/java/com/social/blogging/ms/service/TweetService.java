package com.social.blogging.ms.service;

import com.social.blogging.ms.db.model.Tweets;
import com.social.blogging.ms.db.repository.TweetRepository;
import com.social.blogging.ms.messaging.MessageSender;
import com.social.blogging.ms.service.mapper.TweetMapper;
import com.social.blogging.ms.web.model.TweetRequest;
import com.social.blogging.ms.web.model.TweetResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TweetService {

    private final MessageSender messageSender;
    private final TweetRepository tweetRepository;

    public TweetResponse saveTweet(TweetRequest tweetRequest) {
        try {
            Tweets entity = TweetMapper.toEntity(tweetRequest);
            tweetRepository.save(entity);

            publish(entity);

            log.info("tweet saved: {}", entity);

            return TweetMapper.toWebResponse(entity);
        } catch (Exception e) {
            log.error("error processing tweet: {}", e.getMessage(), e);
            throw e;
        }

    }

    @Async
    public void publish(Tweets savedTweet) {
        try {
            log.info("Sending tweet to SQS: {}", savedTweet);
            messageSender.publish(savedTweet);
        } catch (Exception e) {
            log.error("Error sending message to SQS: {}", e.getMessage(), e);
        }
    }

}
