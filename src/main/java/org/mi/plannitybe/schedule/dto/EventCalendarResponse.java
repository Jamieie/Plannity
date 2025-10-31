package org.mi.plannitybe.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class EventCalendarResponse {
    private Long eventId;
    private Long eventListId;
    private String title;
    private EventDateTime eventDateTime;

    public EventCalendarResponse(Long eventId, Long eventListId, String title, 
                                LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        this.eventId = eventId;
        this.eventListId = eventListId;
        this.title = title;
        this.eventDateTime = EventDateTime.of(startDate, endDate, isAllDay);
    }

    public EventCalendarResponse(Long eventId, Long eventListId, String title, EventDateTime eventDateTime) {
        this.eventId = eventId;
        this.eventListId = eventListId;
        this.title = title;
        this.eventDateTime = eventDateTime;
    }
}