package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service")
public interface CommentClient {
    @GetMapping("/admin/comments")
    List<CommentShortDto> findPageableCommentsForEvent(@RequestParam long eventId,
                                                       @RequestParam int from,
                                                       @RequestParam int size);

    @GetMapping("/admin/comments")
    List<CommentShortDto> findFirstCommentsForEvent(@RequestParam long eventId, @RequestParam long size);

    @GetMapping("/admin/comments")
    Map<Long, Long> getCommentsNumberForEvents(@RequestParam List<Long> eventIds);
}
