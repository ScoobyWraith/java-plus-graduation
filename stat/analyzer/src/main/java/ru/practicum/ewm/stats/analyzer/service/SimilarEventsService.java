package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.analyzer.storage.EventsSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.storage.UserActionsRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarEventsService {
    private final EventsSimilarityRepository eventsSimilarityRepository;
    private final UserActionsRepository userActionsRepository;

    public Map<Long, Double> getSimilarEvents(long userId, long eventId, long maxResults) {
        log.info("Обработка запроса на получение {} похожих событий на {} для юзера {}.", maxResults, eventId, userId);
        List<Long> interactedEventIds = userActionsRepository.getInteractedEventIds(userId);

        log.info("События, с которыми взаимодействовал юзер: {}.", interactedEventIds);

        Map<Long, Double> result = eventsSimilarityRepository.findSimilarEvents(eventId, interactedEventIds, maxResults)
                .stream()
                .collect(
                        Collectors.toMap(
                                es -> es.getEventAId() == eventId ? es.getEventBId() : es.getEventAId(),
                                EventsSimilarity::getScore
                        )
                );

        log.info("Результат обработки: {}.", result);

        return result;
    }
}
