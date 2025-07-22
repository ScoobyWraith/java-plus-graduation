package ru.practicum.ewm.requestservice.service;

import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;

import java.util.List;
import java.util.Map;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createUserRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequest(Long userId, Long requestId);

    RequestShortDto findByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsForEvent(long eventId);

    EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams);

    Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds);
}
