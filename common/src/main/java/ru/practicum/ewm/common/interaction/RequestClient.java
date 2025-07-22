package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {
    @GetMapping("/requests")
    RequestShortDto findByRequesterIdAndEventId(@RequestParam Long userId, @RequestParam Long eventId);

    @GetMapping("/requests")
    List<ParticipationRequestDto> getRequestsForEvent(@RequestParam long eventId);

    @PostMapping("/requests")
    EventRequestStatusUpdateResult updateRequestsForEvent(@RequestBody UpdateRequestsStatusParameters updateParams);
}
