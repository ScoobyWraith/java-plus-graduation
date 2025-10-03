package ru.practicum.ewm.stats.analyzer.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.analyzer.model.EventsSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventsSimilarityRepository extends JpaRepository<EventsSimilarity, Long> {
    Optional<EventsSimilarity> findByEventAIdAndEventBId(long eventA, long eventB);

    @Query(nativeQuery = true, value = """
       SELECT *
       FROM events_similarity es
       WHERE es.event_a_id = ?1 AND es.event_b_id NOT IN (?2)
          OR es.event_b_id = ?1 AND es.event_a_id NOT IN (?2)
       ORDER BY es.score DESC
       LIMIT ?3
       """)
    List<EventsSimilarity> findSimilarEvents(long eventId, List<Long> interactedEventIds, long maxResults);

    @Query(nativeQuery = true, value = """
       SELECT *
       FROM events_similarity es
       WHERE es.event_a_id IN (?1) AND es.event_b_id NOT IN (?1)
          OR es.event_b_id IN (?1) AND es.event_a_id NOT IN (?1)
       ORDER BY es.score DESC
       LIMIT ?2
       """)
    List<EventsSimilarity> findSimilarNotInteractedEvents(List<Long> interactedEventIds, long maxResults);

    @Query(nativeQuery = true, value = """
       SELECT *
       FROM events_similarity es
       WHERE es.event_a_id = ?1 AND es.event_b_id IN (?2)
          OR es.event_b_id = ?1 AND es.event_a_id IN (?2)
       ORDER BY es.score DESC
       LIMIT ?3
       """)
    List<EventsSimilarity> findInteractedSimilarEvents(long eventId, List<Long> interactedEventIds, long maxResults);
}
