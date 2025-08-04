package ru.practicum.ewm.events.views;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.AnalyzerClient;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsRatingGetter {
    private final AnalyzerClient analyzerClient;

    public Map<Long, Double> getEventsRatingMap(List<Long> eventIds) {
        Map<Long, Double> result = analyzerClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        eventIds.forEach(id -> result.putIfAbsent(id, 0.0));
        return result;
    }
}

