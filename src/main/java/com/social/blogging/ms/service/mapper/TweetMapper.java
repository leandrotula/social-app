package com.social.blogging.ms.service.mapper;

import com.social.blogging.ms.db.model.Tweets;
import com.social.blogging.ms.web.model.TweetRequest;
import com.social.blogging.ms.web.model.TweetResponse;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.UUID;

@UtilityClass
public class TweetMapper {
    public Tweets toEntity(TweetRequest request) {
        return Tweets.builder()
                .userId(request.username())
                .content(request.content())
                .tweetId(String.valueOf(UUID.randomUUID().toString()))
                .createdAt(StringUtils.hasLength(request.createdAt()) ? request.createdAt()
                        : String.valueOf(System.currentTimeMillis()))
                .build();
    }

    public TweetResponse toWebResponse(Tweets tweetSaved) {
        return new TweetResponse(tweetSaved.getUserId(), tweetSaved.getContent());
    }
}
