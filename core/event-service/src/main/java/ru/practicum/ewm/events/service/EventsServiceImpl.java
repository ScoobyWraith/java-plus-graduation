package ru.practicum.ewm.events.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;
import ru.practicum.ewm.common.dto.event.EventFullDto;
import ru.practicum.ewm.common.dto.event.EventPublishState;
import ru.practicum.ewm.common.dto.event.LocationDto;
import ru.practicum.ewm.common.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.common.dto.request.UpdateRequestsStatusParameters;
import ru.practicum.ewm.common.dto.user.GetUserShortRequest;
import ru.practicum.ewm.common.dto.user.UserShortDto;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.DataIntegrityViolationException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.exception.ValidationException;
import ru.practicum.ewm.common.interaction.CommentClient;
import ru.practicum.ewm.common.interaction.RequestClient;
import ru.practicum.ewm.common.interaction.UserClient;
import ru.practicum.ewm.common.util.Util;
import ru.practicum.ewm.events.dto.EventFullDtoWithComments;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.events.dto.UpdateEventCommonRequest;
import ru.practicum.ewm.events.dto.UpdateEventUserRequest;
import ru.practicum.ewm.events.dto.parameters.EventsForUserParameters;
import ru.practicum.ewm.events.dto.parameters.GetAllCommentsParameters;
import ru.practicum.ewm.events.dto.parameters.MappingEventParameters;
import ru.practicum.ewm.events.dto.parameters.SearchEventsParameters;
import ru.practicum.ewm.events.dto.parameters.SearchPublicEventsParameters;
import ru.practicum.ewm.events.dto.parameters.UpdateEventParameters;
import ru.practicum.ewm.events.enums.AdminEventAction;
import ru.practicum.ewm.events.enums.SortingEvents;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.QEvent;
import ru.practicum.ewm.events.storage.EventsRepository;
import ru.practicum.ewm.events.views.EventsViewsGetter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;
    private final CategoryRepository categoryRepository;

    private final EventsViewsGetter eventsViewsGetter;

    private final CommentClient commentClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsCreatedByUser(EventsForUserParameters eventsForUserParameters) {
        Long userId = eventsForUserParameters.getUserId();
        Integer from = eventsForUserParameters.getFrom();
        Integer size = eventsForUserParameters.getSize();
        Pageable page = createPageableObject(from, size);
        List<Event> userEvents = eventsRepository.findAllByInitiatorIdIs(userId, page).stream()
                .toList();

        return createEventShortDtoList(userEvents);
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        checkEventDateBeforeHours(newEventDto.getEventDate());
        Category category = getCategoryWithCheck(newEventDto.getCategory());
        Event event = EventMapper.fromNewEventDto(newEventDto, category);
        event.setCreatedOn(Util.getNowTruncatedToSeconds());
        event.setInitiatorId(userId);
        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);
        return createEventFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(UpdateEventParameters updateEventParameters) {
        Long userId = updateEventParameters.getUserId();
        Long eventId = updateEventParameters.getEventId();
        UpdateEventUserRequest updateEventUserRequest = updateEventParameters.getUpdateEventUserRequest();

        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);

        if (!canUserUpdateEvent(event)) {
            throw new DataIntegrityViolationException("Only pending or canceled events can be changed.");
        }

        UpdateEventCommonRequest commonRequest = EventMapper.userUpdateRequestToCommonRequest(updateEventUserRequest);
        updateCommonEventProperties(event, commonRequest);

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> event.setEventPublishState(EventPublishState.CANCELED);
                case SEND_TO_REVIEW -> event.setEventPublishState(EventPublishState.PENDING);
            }
        }

        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId) {
        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);
        return requestClient.getRequestsForEvent(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams) {
        Long userId = updateParams.getUserId();
        Long eventId = updateParams.getEventId();
        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);

        try {
            return requestClient.updateRequestsForEvent(updateParams);
        } catch (FeignException e) {
            if (e.status() == 409) {
                throw new DataIntegrityViolationException(String.format(
                        "Event id=%d is full filled for requests.", eventId
                ));
            }

            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchEvents(SearchEventsParameters searchParams) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        Pageable page = createPageableObject(searchParams.getFrom(), searchParams.getSize());

        if (searchParams.getUsers() != null) {
            conditions.add(event.initiatorId.in(searchParams.getUsers()));
        }

        if (searchParams.getStates() != null) {
            List<EventPublishState> states = searchParams.getStates().stream()
                    .map(EventPublishState::valueOf)
                    .toList();
            conditions.add(event.eventPublishState.in(states));
        }

        if (searchParams.getCategories() != null) {
            conditions.add(event.category.id.in(searchParams.getCategories()));
        }

        if (searchParams.getRangeStart() != null) {
            conditions.add(event.eventDate.after(searchParams.getRangeStart()));
        }

        if (searchParams.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(searchParams.getRangeEnd()));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);
        List<Event> resultEvents = eventsRepository.findAll(condition, page).stream()
                .toList();
        return createEventFullDtoList(resultEvents);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = getEventWithCheck(eventId);
        UpdateEventCommonRequest commonRequest = EventMapper.adminUpdateRequestToCommonRequest(updateRequest);
        updateCommonEventProperties(event, commonRequest);

        if (updateRequest.getStateAction() != null) {
            AdminEventAction stateAction = updateRequest.getStateAction();
            EventPublishState eventPublishState = event.getEventPublishState();

            if (stateAction == AdminEventAction.REJECT_EVENT) {
                if (eventPublishState == EventPublishState.PUBLISHED) {
                    throw new DataIntegrityViolationException("Can't REJECT event which is PUBLISHED already.");
                }

                event.setEventPublishState(EventPublishState.CANCELED);
            } else if (stateAction == AdminEventAction.PUBLISH_EVENT) {
                if (eventPublishState != EventPublishState.PENDING) {
                    throw new DataIntegrityViolationException("Can't PUBLISH event which is not PENDING yet.");
                }

                LocalDateTime now = Util.getNowTruncatedToSeconds();

                if (now.plusHours(1).isAfter(event.getEventDate())) {
                    throw new DataIntegrityViolationException("There are less than 1 hour between publish time and " +
                            "event time.");
                }

                event.setEventPublishState(EventPublishState.PUBLISHED);
                event.setPublishedOn(now);
            }
        }

        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchPublicEvents(SearchPublicEventsParameters searchParams) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.eventPublishState.eq(EventPublishState.PUBLISHED));

        if (searchParams.getText() != null) {
            String text = searchParams.getText();
            conditions.add(event.annotation.containsIgnoreCase(text).or(event.description.containsIgnoreCase(text)));
        }

        if (searchParams.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(searchParams.getCategories());

            if (categories.isEmpty()) {
                throw new ValidationException("Categories from search query are not found.");
            }

            conditions.add(event.category.id.in(searchParams.getCategories()));
        }

        if (searchParams.getOnlyAvailable() != null) {
            List<Long> ids = getAvailableEventIdsByParticipantLimit();
            conditions.add(event.id.in(ids));
        }

        if (searchParams.getPaid() != null) {
            conditions.add(event.paid.eq(searchParams.getPaid()));
        }

        if (searchParams.getRangeStart() != null || searchParams.getRangeEnd() != null) {
            if (searchParams.getRangeStart() != null) {
                conditions.add(event.eventDate.after(searchParams.getRangeStart()));
            }

            if (searchParams.getRangeEnd() != null) {
                conditions.add(event.eventDate.before(searchParams.getRangeEnd()));
            }
        } else {
            LocalDateTime now = Util.getNowTruncatedToSeconds();
            conditions.add(event.eventDate.after(now));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);
        Iterable<Event> resultEvents = eventsRepository.findAll(condition);
        List<Long> resultEventIds = StreamSupport.stream(resultEvents.spliterator(), false)
                .map(Event::getId)
                .toList();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(resultEventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(resultEventIds);
        Comparator<Event> sorting = Comparator.comparing(Event::getEventDate);

        if (searchParams.getSort() == SortingEvents.VIEWS) {
            sorting = (ev1, ev2) -> Long.compare(eventsViewsMap.get(ev2.getId()), eventsViewsMap.get(ev1.getId()));
        } else if (searchParams.getSort() == SortingEvents.COMMENTS) {
            Map<Long, Long> commentsMap = getCommentsNumberMap(resultEventIds);
            sorting = (ev1, ev2) -> Long.compare(commentsMap.get(ev2.getId()), commentsMap.get(ev1.getId()));
        }

        return StreamSupport.stream(resultEvents.spliterator(), false)
                .sorted(sorting)
                .skip(searchParams.getFrom())
                .limit(searchParams.getSize())
                .map(ev -> createEventFullDto(ev, eventsViewsMap.get(ev.getId()), confirmedRequestsMap.get(ev.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithComments getPublicEventById(Long eventId) {
        QEvent event = QEvent.event;
        Event resultEvent = eventsRepository
                .findOne(event.id.eq(eventId).and(event.eventPublishState.eq(EventPublishState.PUBLISHED)))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Event id=%d not found or is not published.", eventId))
                );
        return createEventFullDtoWithComments(resultEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getAllEventComments(GetAllCommentsParameters parameters) {
        Event event = getEventWithCheck(parameters.getEventId());
        return commentClient.findPageableCommentsForEvent(event.getId(), parameters.getFrom(), parameters.getSize());
    }

    @Override
    public EventFullDto getFullEventDtoById(long eventId) {
        Event event = getEventWithCheck(eventId);
        return createEventFullDto(event);
    }

    private Event getEventWithCheck(long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> new ConflictException(String.format("Event id=%d not found.", eventId)));
    }

    private Category getCategoryWithCheck(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ConflictException(String.format("Category id=%d not found.", categoryId)));
    }

    private void checkEventDateBeforeHours(LocalDateTime eventDateTime) {
        LocalDateTime now = Util.getNowTruncatedToSeconds();

        if (eventDateTime.isBefore(now.plusHours(2))) {
            throw new ValidationException("DateTime of event must be ahead more than 2 hours.");
        }
    }

    private void checkUserRights(long userId, Event event) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException(
                    String.format("Access deny for user id=%d with event id=%d.", userId, event.getId())
            );
        }
    }

    private boolean canUserUpdateEvent(Event event) {
        EventPublishState state = event.getEventPublishState();
        return state.equals(EventPublishState.CANCELED) || state.equals(EventPublishState.PENDING);
    }

    private Pageable createPageableObject(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Parameters 'from' and 'size' can not be less then zero");
        }

        return PageRequest.of(from / size, size);
    }

    private void updateCommonEventProperties(Event event, UpdateEventCommonRequest commonProperties) {
        if (commonProperties.getEventDate() != null) {
            checkEventDateBeforeHours(commonProperties.getEventDate());
            event.setEventDate(commonProperties.getEventDate());
        }

        if (commonProperties.getCategory() != null) {
            event.setCategory(getCategoryWithCheck(commonProperties.getCategory()));
        }

        if (commonProperties.getTitle() != null) {
            event.setTitle(commonProperties.getTitle());
        }

        if (commonProperties.getDescription() != null) {
            event.setDescription(commonProperties.getDescription());
        }

        if (commonProperties.getAnnotation() != null) {
            event.setAnnotation(commonProperties.getAnnotation());
        }

        if (commonProperties.getLocation() != null) {
            LocationDto location = commonProperties.getLocation();
            event.setLocationLat(location.getLat());
            event.setLocationLon(location.getLon());
        }

        if (commonProperties.getRequestModeration() != null) {
            event.setRequestModeration(commonProperties.getRequestModeration());
        }

        if (commonProperties.getPaid() != null) {
            event.setPaid(commonProperties.getPaid());
        }

        if (commonProperties.getParticipantLimit() != null) {
            event.setParticipantLimit(commonProperties.getParticipantLimit());
        }
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        Map<Long, Long> confirmed = requestClient.getConfirmedRequestsForEvents(eventIds);

        return eventIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> confirmed.getOrDefault(id, 0L)));
    }

    private Map<Long, Long> getCommentsNumberMap(List<Long> eventIds) {
        return commentClient.getCommentsNumberForEvents(eventIds);
    }

    private EventFullDto createEventFullDto(Event event) {
        long id = event.getId();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(List.of(id));
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(List.of(id));
        Map<Long, UserShortDto> userShortsMap = userClient
                .getUsersShort(new GetUserShortRequest(List.of(event.getInitiatorId())));

        MappingEventParameters eventFullDtoParams = MappingEventParameters.builder()
                .event(event)
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(userShortsMap.get(event.getInitiatorId()))
                .confirmedRequests(confirmedRequestsMap.get(id))
                .views(eventsViewsMap.get(id))
                .build();
        return EventMapper.toEventFullDto(eventFullDtoParams);
    }

    private EventFullDtoWithComments createEventFullDtoWithComments(Event event) {
        long id = event.getId();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(List.of(id));
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(List.of(id));
        List<CommentShortDto> commentsShort = commentClient.findFirstCommentsForEvent(id, 5L);
        Map<Long, UserShortDto> userShortsMap = userClient
                .getUsersShort(new GetUserShortRequest(List.of(event.getInitiatorId())));

        MappingEventParameters eventFullDtoParams = MappingEventParameters.builder()
                .event(event)
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(userShortsMap.get(event.getInitiatorId()))
                .confirmedRequests(confirmedRequestsMap.get(id))
                .views(eventsViewsMap.get(id))
                .comments(commentsShort)
                .build();
        return EventMapper.toEventEventFullDtoWithComments(eventFullDtoParams);
    }

    private EventFullDto createEventFullDto(Event event, long views, long confirmedRequests) {
        Map<Long, UserShortDto> userShortsMap = userClient
                .getUsersShort(new GetUserShortRequest(List.of(event.getInitiatorId())));
        MappingEventParameters eventFullDtoParams = MappingEventParameters.builder()
                .event(event)
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(userShortsMap.get(event.getInitiatorId()))
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
        return EventMapper.toEventFullDto(eventFullDtoParams);
    }

    private List<EventFullDto> createEventFullDtoList(List<Event> events) {
        List<Long> ids = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        events.forEach(event -> {
            ids.add(event.getId());
            userIds.add(event.getInitiatorId());
        });

        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(ids);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(ids);
        Map<Long, UserShortDto> userShortsMap = userClient
                .getUsersShort(new GetUserShortRequest(userIds));

        return events.stream()
                .map(event -> {
                    MappingEventParameters eventFullDtoParams = MappingEventParameters.builder()
                            .event(event)
                            .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                            .initiator(userShortsMap.get(event.getInitiatorId()))
                            .confirmedRequests(confirmedRequestsMap.get(event.getId()))
                            .views(eventsViewsMap.get(event.getId()))
                            .build();
                    return EventMapper.toEventFullDto(eventFullDtoParams);
                })
                .toList();
    }

    private List<EventShortDto> createEventShortDtoList(List<Event> events) {
        List<Long> ids = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        events.forEach(event -> {
            ids.add(event.getId());
            userIds.add(event.getInitiatorId());
        });

        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(ids);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(ids);
        Map<Long, UserShortDto> userShortsMap = userClient
                .getUsersShort(new GetUserShortRequest(userIds));

        return events.stream()
                .map(event -> {
                    MappingEventParameters mappingEventParameters = MappingEventParameters.builder()
                            .event(event)
                            .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                            .initiator(userShortsMap.get(event.getInitiatorId()))
                            .confirmedRequests(confirmedRequestsMap.get(event.getId()))
                            .views(eventsViewsMap.get(event.getId()))
                            .build();

                    return EventMapper.toEventShortDto(mappingEventParameters);
                })
                .toList();
    }

    private List<Long> getAvailableEventIdsByParticipantLimit() {
        List<Long> result = eventsRepository.getEventIdsWithZeroParticipantLimit();
        List<Event> eventsWithNonZeroLimit = eventsRepository.getEventsWithNonZeroParticipantLimit();

        if (!eventsWithNonZeroLimit.isEmpty()) {
            List<Long> ids = eventsWithNonZeroLimit.stream()
                    .map(Event::getId)
                    .toList();
            Map<Long, Long> confirmed = getConfirmedRequestsMap(ids);

            result = Stream.concat(
                    result.stream(),
                    eventsWithNonZeroLimit.stream()
                            .filter(event -> confirmed.get(event.getId()) < event.getParticipantLimit())
                            .map(Event::getId)
            ).toList();
        }

        return result;
    }
}
