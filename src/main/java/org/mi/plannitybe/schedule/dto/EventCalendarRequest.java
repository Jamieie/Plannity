package org.mi.plannitybe.schedule.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventCalendarRequest {

    @NotNull(message = "조회 시작 날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @NotNull(message = "조회 종료 날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    @AssertTrue(message = "조회 시작 날짜는 종료 날짜보다 미래일 수 없습니다.")
    public boolean isValidRange() {
        if (from == null || to == null) {
            return true;
        }
        return !from.isAfter(to);
    }
}
