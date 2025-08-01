package ru.practicum.ewm.events.dto.parameters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;
import ru.practicum.ewm.common.dto.event.CategoryDto;
import ru.practicum.ewm.common.dto.user.UserShortDto;
import ru.practicum.ewm.events.model.Event;

import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MappingEventParameters {
    Event event;
    CategoryDto categoryDto;
    UserShortDto initiator;
    Long confirmedRequests;
    Long views;
    List<CommentShortDto> comments;
}
