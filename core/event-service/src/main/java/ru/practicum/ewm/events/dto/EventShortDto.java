package ru.practicum.ewm.events.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.common.dto.event.CategoryDto;
import ru.practicum.ewm.common.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String annotation;
    String title;
    Long views;
    CategoryDto category;
    Long confirmedRequests;
    UserShortDto initiator;
    LocalDateTime eventDate;
    Boolean paid;
}
