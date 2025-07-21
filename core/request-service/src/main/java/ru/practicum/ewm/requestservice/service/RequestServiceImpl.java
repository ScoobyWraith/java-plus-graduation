package ru.practicum.ewm.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.dto.event.EventFullDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.interaction.EventClient;
import ru.practicum.ewm.requestservice.dto.ParticipationRequestDto;
import ru.practicum.ewm.requestservice.mapper.RequestMapper;
import ru.practicum.ewm.requestservice.model.Request;
import ru.practicum.ewm.common.dto.request.RequestStatus;
import ru.practicum.ewm.requestservice.params.RequestValidator;
import ru.practicum.ewm.requestservice.repository.RequestRepository;
import ru.practicum.ewm.common.util.Util;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createUserRequest(Long userId, Long eventId) {

        EventFullDto event = eventClient.getFullEventDtoById(eventId);

        RequestStatus status = event.getParticipantLimit() == 0 || !event.getRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        RequestValidator validator = new RequestValidator(event, userId, eventId, requestRepository);
        validator.validate();

        Request newRequest = Request.builder()
                .created(Util.getNowTruncatedToSeconds())
                .eventId(eventId)
                .requesterId(userId)
                .status(status)
                .build();
        return RequestMapper.toRequestDto(requestRepository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request not found with id %d", requestId)));

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public RequestShortDto findByRequesterIdAndEventId(Long userId, Long eventId) {
        return null;
    }
}