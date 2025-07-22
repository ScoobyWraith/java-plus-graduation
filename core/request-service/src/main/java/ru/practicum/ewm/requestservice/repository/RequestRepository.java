package ru.practicum.ewm.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.common.dto.request.RequestStatus;
import ru.practicum.ewm.requestservice.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Request findByRequesterIdAndEventId(Long userId, Long eventId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query(nativeQuery = true, value = """
       SELECT r.event_id, SUM(CASE WHEN r.status = 'CONFIRMED' THEN 1 ELSE 0 END)
       FROM requests r
       WHERE r.event_id IN (?1)
       GROUP BY r.event_id
       """)
    List<List<Long>> getConfirmedRequestsForEvents(List<Long> eventIds);
}
