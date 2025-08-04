package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.analyzer.storage.EventsSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.storage.UserActionsRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimilarEventsService {
    private final EventsSimilarityRepository eventsSimilarityRepository;
    private final UserActionsRepository userActionsRepository;

    public Map<Long, Double> getSimilarEvents(long userId, long eventId, long maxResults) {
        List<Long> interactedEventIds = userActionsRepository.getInteractedEventIds(userId);

        return eventsSimilarityRepository.findSimilarEvents(eventId, interactedEventIds, maxResults)
                .stream()
                .collect(
                        Collectors.toMap(
                                es -> es.getEventAId() == eventId ? es.getEventBId() : es.getEventAId(),
                                EventsSimilarity::getScore
                        )
                );
    }
}
