package org.mi.plannitybe.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.time.LocalDateTime;

@Getter
public class EventCalendarResponse {
    private final Long eventId;
    private final Long eventListId;
    private final String title;
    private final EventDateTime eventDateTime;

    public EventCalendarResponse(Long eventId, Long eventListId, String title, 
                                LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        this.eventId = eventId;
        this.eventListId = eventListId;
        this.title = title;
        this.eventDateTime = EventDateTime.of(startDate, endDate, isAllDay);
    }
}