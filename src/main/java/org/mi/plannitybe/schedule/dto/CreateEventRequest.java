package org.mi.plannitybe.schedule.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CreateEventRequest {

    @NotNull(message = "event list는 필수 입력값입니다.")
    private Long eventListId;

    @NotBlank(message = "title은 필수 입력값입니다.")
    private String title;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull
    private Boolean isAllDay;

    private String description;

    private List<Long> taskIds = new ArrayList<>();

    @JsonCreator
    public CreateEventRequest(@JsonProperty("eventListId") Long eventListId,
                              @JsonProperty("title") String title,
                              @JsonProperty("startDate") LocalDateTime startDate,
                              @JsonProperty("endDate") LocalDateTime endDate,
                              @JsonProperty("isAllDay") Boolean isAllDay,
                              @JsonProperty("description") String description,
                              @JsonProperty("tasks") List<Long> tasks) {

        if (!isValidDates(startDate, endDate))
            throw new IllegalArgumentException("입력된 일정의 날짜가 유효하지 않습니다.");

        this.eventListId = eventListId;
        this.title = title.trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.description = description != null ? description.trim() : "";
        if (tasks != null) {
            // 안정성(예상치 못한 부작용 방지)을 위해 참조값을 그대로 넘기지 않고 배열 원소 복사하여 추가
            this.taskIds.addAll(tasks);
        }
    }

    // startDate와 endDate 유효성 검사
    private boolean isValidDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) return true; // 둘 다 null
        if (startDate != null && endDate != null) return !endDate.isBefore(startDate); // 둘 다 값이 존재하면서 endDate가 startDate와 같거나 보다 미래
        return false; // 둘 중 하나만 null인 경우
    }

    // 시작날짜와 종료날짜 모두 값이 존재하는지 반환
    public boolean hasBothDates() {
        return startDate != null && endDate != null;
    }

    // 시작날짜와 종료날짜간의 시간차이를 반환하는 메서드
    public long getDurationInDays() {
        if (hasBothDates()) {
            return Duration.between(startDate, endDate).toDays();
        }
        return 0;
    }

    // 시작날짜의 시간이 00:00:00인지 반환
    public boolean isStartAtMidnight() {
        return startDate != null && startDate.toLocalTime().equals(LocalTime.MIDNIGHT);
    }

    // 종료날짜의 시간이 00:00:00인지 반환
    public boolean isEndAtMidnight() {
        return endDate != null && endDate.toLocalTime().equals(LocalTime.MIDNIGHT);
    }
}
