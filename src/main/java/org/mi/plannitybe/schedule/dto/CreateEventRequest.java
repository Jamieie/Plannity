package org.mi.plannitybe.schedule.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.mi.plannitybe.schedule.domain.EventDateTime;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CreateEventRequest {

    @NotNull(message = "event list는 필수 입력값입니다.")
    private Long eventListId;

    @NotBlank(message = "title은 필수 입력값입니다.")
    private String title;

    @Valid
    @NotNull(message = "날짜 정보는 필수 입력값입니다.")
    private EventDateTime eventDateTime;

    private String description;

    private List<Long> taskIds = new ArrayList<>();

    @JsonCreator
    public CreateEventRequest(@JsonProperty("eventListId") Long eventListId,
                              @JsonProperty("title") String title,
                              @JsonProperty("eventDateTime") EventDateTime eventDateTime,
                              @JsonProperty("description") String description,
                              @JsonProperty("tasks") List<Long> tasks) {
        this.eventListId = eventListId;
        this.title = title != null ? title.trim() : "";
        this.eventDateTime = eventDateTime;
        this.description = description != null ? description.trim() : "";
        if (tasks != null) {
            // 안정성(예상치 못한 부작용 방지)을 위해 참조값을 그대로 넘기지 않고 배열 원소 복사하여 추가
            this.taskIds.addAll(tasks);
        }
    }
}
