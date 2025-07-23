package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service")
public interface CommentClient {
    @GetMapping("/admin/comments/events/{eventId}")
    List<CommentShortDto> findPageableCommentsForEvent(@PathVariable("eventId") long eventId,
                                                       @RequestParam int from,
                                                       @RequestParam int size);

    @GetMapping("/admin/comments/events/{eventId}/first-comments")
    List<CommentShortDto> findFirstCommentsForEvent(@PathVariable("eventId") long eventId,
                                                    @RequestParam long size);

    @GetMapping("/admin/comments/events")
    Map<Long, Long> getCommentsNumberForEvents(@RequestParam List<Long> eventIds);
}
