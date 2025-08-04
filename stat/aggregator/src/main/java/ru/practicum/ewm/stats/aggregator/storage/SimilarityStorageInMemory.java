package ru.practicum.ewm.stats.aggregator.storage;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class SimilarityStorageInMemory implements SimilarityStorage {
    // { [eventId] -> { [userId] -> weight } }
    private final Map<Long, Map<Long, Double>> weightsOfUsersActionsOnEvents = new HashMap<>();

    // { [eventId] -> sum of weights [denominator] }
    private final Map<Long, Double> eventsWeightsSums = new HashMap<>();

    // { [eventId_1] -> { [eventId_2] -> sum of min weights [nominator] } }
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    @Override
    public double getEventWeight(long eventId, long userId) {
        return weightsOfUsersActionsOnEvents
                .computeIfAbsent(eventId, e -> new HashMap<>())
                .getOrDefault(userId, 0.0);
    }

    @Override
    public void setEventWeight(long eventId, long userId, double weight) {
        weightsOfUsersActionsOnEvents
                .computeIfAbsent(eventId, e -> new HashMap<>())
                .put(userId, weight);
    }

    @Override
    public Set<Long> getAllEventIds() {
        return weightsOfUsersActionsOnEvents
                .keySet();
    }

    @Override
    public void setMinWeightsSums(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    @Override
    public double getMinWeightsSums(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    @Override
    public double getEventWeightsSums(long eventId) {
        return eventsWeightsSums
                .getOrDefault(eventId, 0.0);
    }

    @Override
    public void setEventWeightsSums(long eventId, double sum) {
        eventsWeightsSums
                .put(eventId, sum);
    }
}
