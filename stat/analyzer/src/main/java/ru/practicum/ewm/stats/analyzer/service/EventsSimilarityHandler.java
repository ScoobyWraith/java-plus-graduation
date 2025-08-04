package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.mapper.EventsSimilarityMapper;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.analyzer.storage.EventsSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class EventsSimilarityHandler {
    private final EventsSimilarityRepository eventsSimilarityRepository;
    private final EventsSimilarityMapper eventsSimilarityMapper;

    public void handle(EventSimilarityAvro record) {
        EventsSimilarity similarityRecord = eventsSimilarityMapper.fromEventSimilarityAvro(record);
        EventsSimilarity similarityFromRepository = eventsSimilarityRepository
                .findByEventAIdAndEventBId(similarityRecord.getEventAId(), similarityRecord.getEventBId())
                .orElse(similarityRecord);
        similarityFromRepository.setScore(similarityRecord.getScore());
        similarityFromRepository.setTimestamp(similarityRecord.getTimestamp());
        eventsSimilarityRepository.save(similarityFromRepository);
    }
}
