package ru.practicum.ewm.events.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.common.dto.event.CategoryDto;
import ru.practicum.ewm.common.dto.comment.CommentShortDto;
import ru.practicum.ewm.common.dto.event.EventPublishState;
import ru.practicum.ewm.common.dto.event.LocationDto;
import ru.practicum.ewm.common.dto.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDtoWithComments {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;
    LocalDateTime createdOn;
    String description;
    LocalDateTime eventDate;
    UserShortDto initiator;
    LocationDto location;
    Boolean paid;
    Integer participantLimit;
    LocalDateTime publishedOn;
    Boolean requestModeration;
    String title;
    EventPublishState state;
    Long views;
    List<CommentShortDto> comments;
}
