package org.mi.plannitybe.schedule.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.util.ArrayList;
import java.util.List;

public record CreateEventRequest(
    @NotNull(message = "event list는 필수 입력값입니다.")
    Long eventListId,
    
    @NotBlank(message = "title은 필수 입력값입니다.")
    String title,
    
    @Valid
    @NotNull(message = "날짜 정보는 필수 입력값입니다.")
    EventDateTime eventDateTime,
    
    String description,
    
    List<Long> taskIds
) {
    // Compact constructor - 파라미터 처리 로직
    public CreateEventRequest {
        title = title != null ? title.trim() : "";
        description = description != null ? description.trim() : "";
        taskIds = taskIds != null ? new ArrayList<>(taskIds) : new ArrayList<>();
    }
    
    // Jackson용 별도 생성자
    @JsonCreator
    public static CreateEventRequest create(@JsonProperty("eventListId") Long eventListId,
                                           @JsonProperty("title") String title,
                                           @JsonProperty("eventDateTime") EventDateTime eventDateTime,
                                           @JsonProperty("description") String description,
                                           @JsonProperty("taskIds") List<Long> taskIds) {
        return new CreateEventRequest(eventListId, title, eventDateTime, description, taskIds);
    }
}
