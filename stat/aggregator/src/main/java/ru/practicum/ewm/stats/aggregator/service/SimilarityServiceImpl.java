package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.aggregator.storage.SimilarityStorage;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        List<EventSimilarityAvro> result = new ArrayList<>();

        long eventId = userAction.getEventId();
        long userId = userAction.getUserId();

        double newWeight = getWeightByAction(userAction.getActionType());
        double currentWeight = similarityStorage.getEventWeight(eventId, userId);

        // Ничего не делать, если вес не стал больше от действия юзера
        if (newWeight <= currentWeight) {
            return result;
        }

        double currentWeightsSum = similarityStorage.getEventWeightsSums(eventId);
        double newWeightsSum = currentWeightsSum + (newWeight - currentWeight);

        // Обновить weights И сумму weights
        similarityStorage.setEventWeight(eventId, userId, newWeight);
        similarityStorage.setEventWeightsSums(eventId, newWeightsSum);

        Set<Long> allEventIds = similarityStorage.getAllEventIds();

        for (long anotherEventId : allEventIds) {
            if (anotherEventId == eventId) {
                continue;
            }

            double anotherEventWeight = similarityStorage.getEventWeight(anotherEventId, userId);
            double anotherWeightsSum = similarityStorage.getEventWeightsSums(anotherEventId);

            double currentMin = Math.min(currentWeight, anotherEventWeight);
            double newMin = Math.min(newWeight, anotherEventWeight);
            double currentMinSums = similarityStorage.getMinWeightsSums(eventId, anotherEventId);
            double newMinSums = currentMinSums + (newMin - currentMin);

            double currentSimilarity = calcSimilarity(currentMinSums, currentWeightsSum, anotherWeightsSum);
            double newSimilarity = calcSimilarity(newMinSums, newWeightsSum, anotherWeightsSum);

            long first = Math.min(eventId, anotherEventId);
            long second = Math.max(eventId, anotherEventId);

            if (newSimilarity != currentSimilarity) {
                result.add(
                        EventSimilarityAvro.newBuilder()
                                .setEventA(first)
                                .setEventB(second)
                                .setScore(newSimilarity)
                                .setTimestamp(userAction.getTimestamp())
                        .build()
                );
            }

            similarityStorage.setMinWeightsSums(first, second, newMinSums);
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
