package org.mi.plannitybe.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class EventCalendarResponse {
    private Long eventId;
    private Long eventListId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAllDay;

    public EventCalendarResponse(Long eventId, Long eventListId, String title, 
                                LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        this.eventId = eventId;
        this.eventListId = eventListId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
    }
}