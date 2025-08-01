package ru.practicum.ewm.requestservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.RequestShortDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;
import ru.practicum.ewm.common.interaction.RequestClient;
import ru.practicum.ewm.requestservice.service.RequestService;

import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.requestservice.constants.RequestConstants.REQUESTS;
import static ru.practicum.ewm.requestservice.constants.RequestConstants.REQUEST_BASE_PATCH_PATH;
import static ru.practicum.ewm.requestservice.constants.RequestConstants.REQUEST_BASE_PATH;
import static ru.practicum.ewm.requestservice.constants.RequestConstants.REQUEST_ID;
import static ru.practicum.ewm.requestservice.constants.RequestConstants.USERS;
import static ru.practicum.ewm.requestservice.constants.RequestConstants.USER_ID;

@Slf4j
@RestController
@RequestMapping(USERS)
@RequiredArgsConstructor
public final class RequestController implements RequestClient {

    private final RequestService requestService;

    @GetMapping(REQUEST_BASE_PATH)
    List<ParticipationRequestDto> getUserRequests(@PathVariable(USER_ID) Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping(REQUEST_BASE_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto createUserRequest(@PathVariable(USER_ID) Long userId,
                                              @RequestParam Long eventId) {
        log.info("Creating request for user with ID: {} for event ID: {}", userId, eventId);
        return requestService.createUserRequest(userId, eventId);
    }


    @PatchMapping(REQUEST_BASE_PATCH_PATH)
    ParticipationRequestDto cancelUserRequest(@PathVariable(USER_ID) Long userId,
                                              @PathVariable(REQUEST_ID) Long requestId) {
        log.info("Cancelling request with ID: {} for user with ID: {}", requestId, userId);
        return requestService.cancelUserRequest(userId, requestId);

    }

    @Override
    @GetMapping(REQUESTS)
    public RequestShortDto findByRequesterIdAndEventId(@RequestParam Long userId, @RequestParam Long eventId) {
        return requestService.findByRequesterIdAndEventId(userId, eventId);
    }

    @Override
    @GetMapping(REQUESTS + "/event/{eventId}")
    public List<ParticipationRequestDto> getRequestsForEvent(@PathVariable("eventId") long eventId) {
        return requestService.getRequestsForEvent(eventId);
    }

    @Override
    @PostMapping(REQUESTS)
    public EventRequestStatusUpdateResult updateRequestsForEvent(@RequestBody UpdateRequestsStatusParameters updateParams) {
        return requestService.updateRequestsForEvent(updateParams);
    }

    @Override
    @GetMapping(REQUESTS + "/event/confirmed-quantity")
    public Map<Long, Long> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequestsForEvents(eventIds);
    }
}
