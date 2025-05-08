package com.social.blogging.ms.web.controller;

import com.social.blogging.ms.service.FollowService;
import com.social.blogging.ms.service.TimeLineService;
import com.social.blogging.ms.service.TweetService;
import com.social.blogging.ms.web.model.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class BloggingController {

    private final TweetService tweetService;
    private final FollowService followService;
    private final TimeLineService timeLineService;

    @PostMapping(value = "/tweets")
    public ResponseEntity<TweetResponse> tweet(@RequestBody @Valid TweetRequest tweetRequest) {
        TweetResponse tweetResponse = tweetService.saveTweet(tweetRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tweetResponse);

    }

    @PostMapping("/followers")
    public ResponseEntity<FollowerResponse> follow(@RequestBody @Valid FollowerRequest followerRequest) {
        FollowerResponse created = followService.createFollow(followerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/timeline/{followedId}")
    public ResponseEntity<TimeLineResponse> getTimelineByFollowedId(@PathVariable String followedId) {
        return ResponseEntity.ok(timeLineService.getTimelineByFollowedId(followedId));
    }

}
