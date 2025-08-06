package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.aggregator.storage.SimilarityStorage;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {
    @Value("${similarity.weights.like}")
    private double likeWeight;

    @Value("${similarity.weights.register}")
    private double registerWeight;

    @Value("${similarity.weights.view}")
    private double viewWeight;

    private final SimilarityStorage similarityStorage;

    @Override
    public List<EventSimilarityAvro> getSimilarityEvents(UserActionAvro userAction) {
        log.info("Запрос на пересчет схожести. Поступили данные: {}.", userAction);

        List<EventSimilarityAvro> result = new ArrayList<>();

        long eventId = userAction.getEventId();
        long userId = userAction.getUserId();

        double newWeight = getWeightByAction(userAction.getActionType());
        double currentWeight = similarityStorage.getEventWeight(eventId, userId);

        log.info("Текущий вес: {}. Новый вес: {}.", currentWeight, newWeight);

        double currentWeightsSum = similarityStorage.getEventWeightsSums(eventId);
        double newWeightsSum = currentWeightsSum + (newWeight - currentWeight);

        log.info("Текущая сумма весов: {}. Новая сумма весов: {}.", currentWeightsSum, newWeightsSum);

        // Обновить weights И сумму weights
        similarityStorage.setEventWeight(eventId, userId, newWeight);
        similarityStorage.setEventWeightsSums(eventId, newWeightsSum);

        // Ничего не делать, если вес не стал больше от действия юзера
        if (newWeight <= currentWeight) {
            return result;
        }

        Set<Long> allEventIds = similarityStorage.getAllEventIds();

        for (long anotherEventId : allEventIds) {
            if (anotherEventId == eventId) {
                continue;
            }

            log.info("Сравнение события {} с событием {}.", eventId, anotherEventId);

            double anotherEventWeight = similarityStorage.getEventWeight(anotherEventId, userId);
            double anotherWeightsSum = similarityStorage.getEventWeightsSums(anotherEventId);
            log.info("Веса события {}: {}, сумма {}.", anotherEventId, anotherEventWeight, anotherWeightsSum);

            double currentMin = Math.min(currentWeight, anotherEventWeight);
            double newMin = Math.min(newWeight, anotherEventWeight);
            log.info("Текущий минимум {}, новый {}.", currentMin, newMin);

            double currentMinSums = similarityStorage.getMinWeightsSums(eventId, anotherEventId);
            double newMinSums = currentMinSums + (newMin - currentMin);
            log.info("Текущая сумма минимумов {}, новая {}.", currentMinSums, newMinSums);

            // Обновить сумму минимальных весов
            similarityStorage.setMinWeightsSums(eventId, anotherEventId, newMinSums);

            double currentSimilarity = 0;
            double newSimilarity = 0;

            if (currentMinSums != 0 && currentWeightsSum != 0) {
                currentSimilarity = calcSimilarity(currentMinSums, currentWeightsSum, anotherWeightsSum);
            }

            if (newMinSums != 0) {
                newSimilarity = calcSimilarity(newMinSums, newWeightsSum, anotherWeightsSum);
            }

            log.info("Текущая схожесть событий {}, новая {}.", currentSimilarity, newSimilarity);

            long first = Math.min(eventId, anotherEventId);
            long second = Math.max(eventId, anotherEventId);

            if (newSimilarity != currentSimilarity) {
                EventSimilarityAvro data = EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(newSimilarity)
                        .setTimestamp(userAction.getTimestamp())
                        .build();

                log.info("Схожесть изменилась. Добавлены данные о схожести: {}.", data);

                result.add(data);
            }
        }

        return result;
    }

    private double calcSimilarity(double minSums, double sumA, double sumB) {
        return minSums / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }

    private double getWeightByAction(ActionTypeAvro action) {
        return switch (action) {
            case LIKE -> likeWeight;
            case REGISTER -> registerWeight;
            case VIEW -> viewWeight;
            default -> 0;
        };
    }
}
