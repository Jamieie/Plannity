package org.mi.plannitybe.schedule.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@EqualsAndHashCode
@ToString
public class EventDateTime {

    @NotNull(message = "시작 날짜는 필수 입력값입니다.")
    private final LocalDateTime startDate;
    @NotNull(message = "종료 날짜는 필수 입력값입니다.")
    private final LocalDateTime endDate;
    private final Boolean isAllDay;

    private EventDateTime(LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay != null ? isAllDay : false;
    }

    @JsonCreator
    public static EventDateTime fromJson(@JsonProperty("startDate") LocalDateTime startDate,
                         @JsonProperty("endDate") LocalDateTime endDate,
                         @JsonProperty("isAllDay") Boolean isAllDay) {
        return new EventDateTime(startDate, endDate, isAllDay);
    }

    public static EventDateTime of(LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        EventDateTime eventDateTime = new EventDateTime(startDate, endDate, isAllDay);
        eventDateTime.validateOrThrow();
        return eventDateTime;
    }


    private void validateOrThrow() {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("일정의 날짜는 반드시 존재해야 합니다.");
        }
        if (!isValidDateCombination()) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜보다 과거일 수 없습니다.");
        }
        if (!isValidAllDayConditions()) {
            throw new IllegalArgumentException("종일일정의 날짜가 올바르지 않습니다.");
        }
    }

    @JsonIgnore
    @AssertTrue(message = "종료 날짜는 시작 날짜보다 과거일 수 없습니다.")
    public boolean isValidDateCombination() {
        if (startDate == null || endDate == null) { return true; }
        return !endDate.isBefore(startDate);
    }

    @JsonIgnore
    @AssertTrue(message = "종일일정의 날짜가 올바르지 않습니다.")
    public boolean isValidAllDayConditions() {
        if (startDate == null || endDate == null) { return true; }
        if (!isAllDay) return true;
        return startDate.toLocalTime().equals(LocalTime.MIDNIGHT) &&   // 시작날짜의 시간이 00:00:00
                endDate.toLocalTime().equals(LocalTime.MIDNIGHT) &&   // 종료날짜의 시간이 00:00:00
                !(Duration.between(startDate, endDate).toDays() < 1);   // 시작날짜와 종료날짜의 시간 차가 하루 이상
    }
}