package com.social.blogging.ms.service.mapper;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.web.model.FollowerRequest;
import com.social.blogging.ms.web.model.FollowerResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FollowerMapper {

    public FollowerResponse toWebResponse(Follow follow) {
        return new FollowerResponse(follow.getFollowerId(), follow.getFolloweeId());
    }

    public Follow toEntity(FollowerRequest request) {
        return new Follow(request.authorUsername(), request.followed(), System.currentTimeMillis());
    }
}
