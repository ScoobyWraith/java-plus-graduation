package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service")
public interface CommentClient {
    List<CommentShortDto> findPageableCommentsForEvent(long eventId, int from, int size);

    List<CommentShortDto> findFirstCommentsForEvent(long eventId, long size);

    Map<Long, Long> getCommentsNumberForEvents(List<Long> eventIds);
}
