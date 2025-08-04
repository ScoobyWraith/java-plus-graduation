package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.storage.UserActionsRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionsService {
    private final UserActionsRepository userActionsRepository;

    public Map<Long, Double> getEventInteractionsSums(List<Long> eventIds) {
        Map<Long, Double> weights = userActionsRepository.getEventInteractionsSums(eventIds)
                .stream()
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        eventIds
                .forEach(id -> weights.putIfAbsent(id, 0.0));
        return weights;
    }
}
