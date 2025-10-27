package org.mi.plannitybe.schedule.dto;

import lombok.*;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class EventResponse {
    private Long id;
    private Long eventListId;
    private String title;
    private EventDateTime eventDateTime;
    private String description;
    private List<Long> taskIds;

    @Builder
    public EventResponse(Long id, Long eventListId, String title, EventDateTime eventDateTime, String description, List<Long> taskIds) {
        this.id = id;
        this.eventListId = eventListId;
        this.title = title;
        this.eventDateTime = eventDateTime;
        this.description = description;
        this.taskIds = taskIds;
    }
}
