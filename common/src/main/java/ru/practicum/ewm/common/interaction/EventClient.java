package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.event.EventFullDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/events/{eventId}/full-dto")
    EventFullDto getFullEventDtoById(@PathVariable("eventId") long eventId);

    @GetMapping("/events/confirmed-requests")
    Map<Long, Long> getConfirmedRequestsMap(@RequestParam List<Long> eventIds);
}
