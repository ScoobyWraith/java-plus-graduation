package ru.practicum.ewm.stats.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class UserActionMapper {
    public UserAction fromUserActionAvro(UserActionAvro userActionAvro, double weight) {
        LocalDateTime ts = LocalDateTime.ofInstant(userActionAvro.getTimestamp(), ZoneOffset.UTC);

        return UserAction.builder()
                .eventId(userActionAvro.getEventId())
                .userId(userActionAvro.getUserId())
                .weight(weight)
                .timestamp(ts)
                .build();
    }
}
