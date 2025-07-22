package ru.practicum.ewm.events.service;

import ru.practicum.ewm.common.dto.comment.CommentShortDto;
import ru.practicum.ewm.common.dto.event.EventFullDto;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;
import ru.practicum.ewm.events.dto.EventFullDtoWithComments;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.events.dto.parameters.EventsForUserParameters;
import ru.practicum.ewm.events.dto.parameters.GetAllCommentsParameters;
import ru.practicum.ewm.events.dto.parameters.SearchEventsParameters;
import ru.practicum.ewm.events.dto.parameters.SearchPublicEventsParameters;
import ru.practicum.ewm.events.dto.parameters.UpdateEventParameters;

import java.util.List;
import java.util.Map;

public interface EventsService {
    List<EventShortDto> getEventsCreatedByUser(EventsForUserParameters eventsForUserParameters);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(UpdateEventParameters updateEventRequestParams);

    List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams);

    List<EventFullDto> searchEvents(SearchEventsParameters searchEventsParameters);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> searchPublicEvents(SearchPublicEventsParameters searchPublicEventsParameters);

    EventFullDtoWithComments getPublicEventById(Long eventId);

    List<CommentShortDto> getAllEventComments(GetAllCommentsParameters parameters);

    EventFullDto getFullEventDtoById(long eventId);

    Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds);
}
