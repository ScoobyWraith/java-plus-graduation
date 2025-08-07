package ru.practicum.ewm.stats.aggregator.storage;

import java.util.Set;

public interface SimilarityStorage {
    double getEventWeight(long eventId, long userId);

    void setEventWeight(long eventId, long userId, double weight);

    Set<Long> getAllEventIds();

    void setMinWeightsSums(long eventA, long eventB, double sum);

    double getMinWeightsSums(long eventA, long eventB);

    double getEventWeightsSums(long eventId);

    void setEventWeightsSums(long eventId, double sum);
}
