package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.ewm.common.dto.event.EventFullDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/events")
    EventFullDto getFullEventDtoById(@RequestParam long eventId);

    @GetMapping("/events")
    Map<Long, Long> getConfirmedRequestsMap(@RequestParam List<Long> eventIds);
}
