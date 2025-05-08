package com.social.blogging.ms.service;

import com.social.blogging.ms.db.model.Follow;
import com.social.blogging.ms.db.repository.FollowerRepository;
import com.social.blogging.ms.service.mapper.FollowerMapper;
import com.social.blogging.ms.web.model.FollowerRequest;
import com.social.blogging.ms.web.model.FollowerResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FollowService {

    private final FollowerRepository followRepository;

    public FollowerResponse createFollow(FollowerRequest follow) {

        Follow save = followRepository.save(FollowerMapper.toEntity(follow));
        return FollowerMapper.toWebResponse(save);
    }

}
