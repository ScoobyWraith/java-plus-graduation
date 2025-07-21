package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.common.dto.event.EventFullDto;

@FeignClient(name = "event-service")
public interface EventClient {
    EventFullDto getFullEventDtoById(long eventId);
}
