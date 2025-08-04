package ru.practicum.ewm.stats.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.LocalDateTime;

@Component
public class EventsSimilarityMapper {
    public EventsSimilarity fromEventSimilarityAvro(EventSimilarityAvro eventSimilarityAvro) {
        return EventsSimilarity.builder()
                .eventAId(eventSimilarityAvro.getEventA())
                .eventBId(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .timestamp(LocalDateTime.from(eventSimilarityAvro.getTimestamp()))
                .build();
    }

}
