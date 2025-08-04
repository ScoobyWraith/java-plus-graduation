package ru.practicum.ewm.stats.analyzer.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Pair;
import ru.practicum.ewm.stats.analyzer.model.UserAction;

import java.util.List;
import java.util.Optional;

public interface UserActionsRepository extends JpaRepository<UserAction, Long> {
    Optional<UserAction> findByEventIdAndUserId(long eventId, long userId);

    @Query(nativeQuery = true, value = """
       SELECT ua.event_id, SUM(ua.weight)
       FROM user_actions ua
       WHERE ua.event_id IN (?1)
       GROUP BY ua.event_id
       """)
    List<Pair<Long, Double>> getEventInteractionsSums(List<Long> eventIds);

    @Query(nativeQuery = true, value = """
       SELECT ua.event_id
       FROM user_actions ua
       WHERE ua.user_id = ?1
       ORDER BY ua.ts DESC
       LIMIT ?2
       """)
    List<Long> getLastInteractedEventIds(long userId, long limit);

    @Query(nativeQuery = true, value = """
       SELECT *
       FROM user_actions ua
       WHERE ua.user_id = ?1
       """)
    List<UserAction> getAllUserActionsWithEvents(long userId);

    @Query(nativeQuery = true, value = """
       SELECT ua.event_id
       FROM user_actions ua
       WHERE ua.user_id = (?1)
       GROUP BY ua.event_id
       """)
    List<Long> getInteractedEventIds(long userId);
}
