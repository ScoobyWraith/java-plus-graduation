package ru.practicum.ewm.common.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Builder(toBuilder = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRequestsStatusParameters {
    Long userId;
    Long eventId;
    EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest;
}
