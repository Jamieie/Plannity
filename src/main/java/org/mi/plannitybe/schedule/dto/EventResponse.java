package org.mi.plannitybe.schedule.dto;

import lombok.*;
import org.mi.plannitybe.schedule.entity.EventTask;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class EventResponse {
    private Long id;
    private Long eventListId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAllDay;
    private String description;
    private List<Long> eventTaskIds;

    @Builder
    public EventResponse(Long id, Long eventListId, String title, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, String description, List<Long> eventTaskIds) {
        this.id = id;
        this.eventListId = eventListId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.description = description;
        this.eventTaskIds = eventTaskIds;
    }
}
