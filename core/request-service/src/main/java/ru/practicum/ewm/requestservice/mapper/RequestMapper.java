package ru.practicum.ewm.requestservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.common.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.requestservice.model.Request;

@Component
public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().toString())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().name())
                .build();
    }
}