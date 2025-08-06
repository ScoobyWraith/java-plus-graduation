package ru.practicum.ewm.stats.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class EventsSimilarityMapper {
    public EventsSimilarity fromEventSimilarityAvro(EventSimilarityAvro eventSimilarityAvro) {
        LocalDateTime ts = LocalDateTime.ofInstant(eventSimilarityAvro.getTimestamp(), ZoneOffset.UTC);

        return EventsSimilarity.builder()
                .eventAId(eventSimilarityAvro.getEventA())
                .eventBId(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .timestamp(ts)
                .build();
    }

}
