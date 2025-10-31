package org.mi.plannitybe.schedule.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.util.ArrayList;
import java.util.List;

public record UpdateEventRequest(Long eventListId, String title, @Valid EventDateTime eventDateTime, String description,
                                 List<Long> taskIds) {

    @JsonCreator
    public UpdateEventRequest(@JsonProperty("eventListId") Long eventListId,
                              @JsonProperty("title") String title,
                              @JsonProperty("eventDateTime") EventDateTime eventDateTime,
                              @JsonProperty("description") String description,
                              @JsonProperty("taskIds") List<Long> taskIds) {
        this.eventListId = eventListId;
        this.title = title == null || title.trim().isEmpty() ? null : title.trim();
        this.eventDateTime = eventDateTime;
        this.description = description == null ? null : description.trim();
        this.taskIds = taskIds == null ? null : new ArrayList<>(taskIds);
    }
}
