package ru.practicum.ewm.requestservice.service;

import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.requestservice.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createUserRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequest(Long userId, Long requestId);

    RequestShortDto findByRequesterIdAndEventId(Long userId, Long eventId);
}
