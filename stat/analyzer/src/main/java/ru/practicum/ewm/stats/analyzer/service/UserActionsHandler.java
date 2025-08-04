package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.mapper.UserActionMapper;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.analyzer.storage.UserActionsRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionsHandler {
    private final UserActionsRepository userActionsRepository;
    private final UserActionMapper userActionMapper;

    @Value("${similarity.weights.like}")
    private double likeWeight;

    @Value("${similarity.weights.register}")
    private double registerWeight;

    @Value("${similarity.weights.view}")
    private double viewWeight;

    public void handle(UserActionAvro record) {
        double weight = getWeightByAction(record.getActionType());
        UserAction recordedAction = userActionMapper.fromUserActionAvro(record, weight);

        UserAction actionFromRepository = userActionsRepository
                .findByEventIdAndUserId(recordedAction.getEventId(), recordedAction.getUserId())
                .orElse(recordedAction);

        actionFromRepository.setWeight(recordedAction.getWeight());
        actionFromRepository.setTimestamp(recordedAction.getTimestamp());
        userActionsRepository.save(actionFromRepository);
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
