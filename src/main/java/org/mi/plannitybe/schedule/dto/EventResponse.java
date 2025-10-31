package org.mi.plannitybe.schedule.dto;

import lombok.*;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.util.List;

public record EventResponse(Long id, Long eventListId, String title, EventDateTime eventDateTime, String description,
                            List<Long> taskIds) {
    @Builder
    public EventResponse {
    }
}
