package ru.practicum.ewm.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.dto.event.EventFullDto;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.RequestStatus;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;
import ru.practicum.ewm.common.dto.request.UserUpdateRequestAction;
import ru.practicum.ewm.common.exception.DataIntegrityViolationException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.interaction.EventClient;
import ru.practicum.ewm.common.util.Util;
import ru.practicum.ewm.requestservice.mapper.RequestMapper;
import ru.practicum.ewm.requestservice.model.Request;
import ru.practicum.ewm.requestservice.params.RequestValidator;
import ru.practicum.ewm.requestservice.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<ParticipationRequestDto> getRequestsForEvent(long eventId) {
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams) {
        Long eventId = updateParams.getEventId();
        EventFullDto event = eventClient.getFullEventDtoById(eventId);
        EventRequestStatusUpdateRequest statusUpdateRequest = updateParams.getEventRequestStatusUpdateRequest();
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        UserUpdateRequestAction action = statusUpdateRequest.getStatus();
        List<Request> requests = requestRepository.findAllById(statusUpdateRequest.getRequestIds());
        Long confirmedRequests = eventClient.getConfirmedRequestsMap(List.of(eventId)).get(eventId);
        Integer participantLimit = event.getParticipantLimit();

        long canConfirmRequestsNumber = participantLimit == 0
                ? requests.size()
                : participantLimit - confirmedRequests;

        if (canConfirmRequestsNumber <= 0) {
            throw new DataIntegrityViolationException(String.format(
                    "Event id=%d is full filled for requests.", eventId
            ));
        }

        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new DataIntegrityViolationException(String.format(
                        "Request id=%d must have status PENDING.", request.getId()
                ));
            }
        });

        for (Request request : requests) {
            if (action == UserUpdateRequestAction.REJECTED || canConfirmRequestsNumber <= 0) {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(RequestMapper.toRequestDto(request));
                continue;
            }

            request.setStatus(RequestStatus.CONFIRMED);
            result.getConfirmedRequests().add(RequestMapper.toRequestDto(request));
            canConfirmRequestsNumber--;
        }

        requestRepository.saveAll(requests);
        return result;
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        return requestRepository.getConfirmedRequestsForEvents(eventIds).stream()
                .collect(Collectors.toMap(List::getFirst, List::getLast));
    }
}