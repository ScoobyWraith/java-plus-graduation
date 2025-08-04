package ru.practicum.ewm.stats.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.LocalDateTime;

@Component
public class UserActionMapper {
    public UserAction fromUserActionAvro(UserActionAvro userActionAvro, double weight) {
        return UserAction.builder()
                .eventId(userActionAvro.getEventId())
                .userId(userActionAvro.getUserId())
                .weight(weight)
                .timestamp(LocalDateTime.from(userActionAvro.getTimestamp()))
                .build();
    }
}
