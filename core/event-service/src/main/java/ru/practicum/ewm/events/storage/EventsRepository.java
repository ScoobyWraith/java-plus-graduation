package ru.practicum.ewm.events.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.events.model.Event;

import java.util.List;

public interface EventsRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByInitiatorIdIs(Long userId, Pageable pageable);

    long countByCategoryId(Long categoryId);

    @Query(nativeQuery = true, value = """
       SELECT e.id
       FROM events e
       WHERE e.participant_limit = 0
       """)
    List<Long> getEventIdsWithZeroParticipantLimit();

    @Query(nativeQuery = true, value = """
       SELECT *
       FROM events e
       WHERE e.participant_limit > 0
       """)
    List<Event> getEventsWithNonZeroParticipantLimit();
}
