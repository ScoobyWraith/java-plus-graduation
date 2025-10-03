package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.analyzer.storage.EventsSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.storage.UserActionsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsService {
    private final EventsSimilarityRepository eventsSimilarityRepository;
    private final UserActionsRepository userActionsRepository;

    public Map<Long, Double> getRecommendations(long userId, long maxResults) {
        log.info("Запрос на {} рекомендаций для юзера {}.", maxResults, userId);
        Map<Long, Double> result = new HashMap<>();
        List<Long> lastInteractedEventIds = userActionsRepository
                .getLastInteractedEventIds(userId, maxResults);

        log.info("Крайние события, с которыми взаимодействовал юзер: {}.", lastInteractedEventIds);

        if (lastInteractedEventIds.isEmpty()) {
            return result;
        }

        List<UserAction> allUserActionsWithEvents = userActionsRepository
                .getAllUserActionsWithEvents(userId);
        Map<Long, Double> userInteractionWeightsForEvents = new HashMap<>();
        List<Long> allInteractedEventIds = new ArrayList<>();

        for (UserAction action : allUserActionsWithEvents) {
            userInteractionWeightsForEvents.put(action.getEventId(), action.getWeight());
            allInteractedEventIds.add(action.getEventId());
        }

        log.info("Веса для всех событий, с которыми взаимодействовал юзер: {}.", userInteractionWeightsForEvents);

        List<Long> similarNotInteractedEventIds = eventsSimilarityRepository
                .findSimilarNotInteractedEvents(lastInteractedEventIds, maxResults)
                .stream()
                .map(se -> lastInteractedEventIds.contains(se.getEventAId()) ? se.getEventBId() : se.getEventAId())
                .toList();

        log.info("События, с которыми НЕ взаимодействовал юзер: {}.", similarNotInteractedEventIds);

        for (long eventId : similarNotInteractedEventIds) {
            log.info("Подсчет предполагаемой оценки для события {}.", eventId);

            Map<Long, EventsSimilarity> similarInteractedEvents = eventsSimilarityRepository.
                    findInteractedSimilarEvents(eventId, allInteractedEventIds, maxResults)
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    es -> es.getEventAId() == eventId ? es.getEventBId() : es.getEventAId(),
                                    Function.identity()
                            )
                    );

            log.info("Мапа схожести событий с рассматриваемым: {}.", similarInteractedEvents);

            double sumOfWeights = 0;
            double sumOfSimilarity = 0;

            for (long interactedEventId : similarInteractedEvents.keySet()) {
                double similarity = similarInteractedEvents.get(interactedEventId).getScore();
                sumOfWeights += similarity * userInteractionWeightsForEvents.get(interactedEventId);
                sumOfSimilarity += similarity;
            }

            log.info("Предполагаемая оценка для события {}: {}.", eventId, sumOfWeights / sumOfSimilarity);

            result.put(eventId, sumOfWeights / sumOfSimilarity);
        }

        return result;
    }
}
