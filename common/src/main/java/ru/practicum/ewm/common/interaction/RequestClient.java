package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.common.dto.request.RequestShortDto;

@FeignClient(name = "request-service")
public interface RequestClient {
    RequestShortDto findByRequesterIdAndEventId(Long userId, Long eventId);
}
