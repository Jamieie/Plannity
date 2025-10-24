package org.mi.plannitybe.schedule.repository;

import org.mi.plannitybe.schedule.dto.EventCalendarResponse;
import org.mi.plannitybe.schedule.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findTopByOrderByIdDesc();

    @Query("SELECT new org.mi.plannitybe.schedule.dto.EventCalendarResponse(" +
           "e.id, el.id, e.title, e.startDate, e.endDate, e.isAllDay) " +
           "FROM Event e " +
           "JOIN e.eventList el " +
           "WHERE el.user.id = :userId " +
           "AND e.startDate IS NOT NULL " +
           "AND e.endDate IS NOT NULL " +
           "AND e.startDate <= :to " +
           "AND e.endDate >= :from " +
           "ORDER BY e.startDate ASC, e.id ASC")
    List<EventCalendarResponse> findEventsByUserIdAndDateRange(@Param("userId") String userId,
                                                              @Param("from") LocalDateTime from,
                                                              @Param("to") LocalDateTime to);
}
