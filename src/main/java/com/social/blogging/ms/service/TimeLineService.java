package com.social.blogging.ms.service;

import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.db.repository.TimeLineRepository;
import com.social.blogging.ms.service.mapper.TimeLineMapper;
import com.social.blogging.ms.web.model.TimeLineItemResponse;
import com.social.blogging.ms.web.model.TimeLineResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TimeLineService {

    private final TimeLineRepository timeLineRepository;

    public TimeLineResponse getTimelineByFollowedId(String followedId) {
        List<Timeline> timelineByFollowedId = timeLineRepository.getTimelineByFollowedId(followedId);
        List<TimeLineItemResponse> fullTimeLineResponse = timelineByFollowedId.stream().map(TimeLineMapper::toWebResponse).toList();

        return new TimeLineResponse(fullTimeLineResponse);
    }
}
