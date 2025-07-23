package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service")
public interface RequestClient {
    @GetMapping("/users/requests")
    RequestShortDto findByRequesterIdAndEventId(@RequestParam Long userId, @RequestParam Long eventId);

    @GetMapping("/users/requests/event/{eventId}")
    List<ParticipationRequestDto> getRequestsForEvent(@PathVariable("eventId") long eventId);

    @PostMapping("/users/requests")
    EventRequestStatusUpdateResult updateRequestsForEvent(@RequestBody UpdateRequestsStatusParameters updateParams);

    @GetMapping("/users/requests/event/confirmed-quantity")
    Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds);
}
