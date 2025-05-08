package com.social.blogging.ms.service.mapper;

import com.social.blogging.ms.db.model.Timeline;
import com.social.blogging.ms.web.model.TimeLineItemResponse;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class TimeLineMapper {

    public TimeLineItemResponse toWebResponse(Timeline timeline) {
        return new TimeLineItemResponse(timeline.getContent(), timeline.getAuthorId(), toDate(timeline.getCreatedAt()));
    }

    private String toDate(Long createdAt) {
        Instant instant = Instant.ofEpochMilli(createdAt);
        LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date.format(formatter);

    }
}
