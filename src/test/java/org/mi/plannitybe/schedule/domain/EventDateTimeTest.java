package org.mi.plannitybe.schedule.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EventDateTime 테스트")
class EventDateTimeTest {

    // 테스트 상수
    private static final LocalDateTime START_DATE = LocalDateTime.of(2024, 1, 1, 10, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2024, 1, 1, 12, 0);
    private static final LocalDateTime MIDNIGHT_START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime MIDNIGHT_END = LocalDateTime.of(2024, 1, 2, 0, 0);

    // ================ 정상 생성 테스트 ================

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'일반 일정', '2024-01-01T10:00', '2024-01-01T12:00', false",
            "'종일 일정', '2024-01-01T00:00', '2024-01-02T00:00', true"
    })
    @DisplayName("정상 생성")
    void create_success(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.of(startDate, endDate, isAllDay);

        // THEN
        assertThat(eventDateTime.getStartDate()).isEqualTo(startDate);
        assertThat(eventDateTime.getEndDate()).isEqualTo(endDate);
        assertThat(eventDateTime.getIsAllDay()).isEqualTo(isAllDay);
    }

    @Test
    @DisplayName("정상 생성 - isAllDay null일 때 기본값 false")
    void create_success_isAllDayNullDefaultFalse() {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.of(START_DATE, END_DATE, null);

        // THEN
        assertThat(eventDateTime.getIsAllDay()).isFalse();
    }

    // ================ 예외 테스트 ================

    @Test
    @DisplayName("생성 실패 - startDate가 null")
    void create_fail_startDateNull() {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EventDateTime.of(null, END_DATE, false));
        assertThat(exception.getMessage()).isEqualTo("일정의 날짜는 반드시 존재해야 합니다.");
    }

    @Test
    @DisplayName("생성 실패 - endDate가 null")
    void create_fail_endDateNull() {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EventDateTime.of(START_DATE, null, false));
        assertThat(exception.getMessage()).isEqualTo("일정의 날짜는 반드시 존재해야 합니다.");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'1초 차이', '2024-01-01T10:00:01', '2024-01-01T10:00:00'",
            "'1분 차이', '2024-01-01T10:01:00', '2024-01-01T10:00:00'",
            "'1시간 차이', '2024-01-01T11:00:00', '2024-01-01T10:00:00'",
            "'1일 차이', '2024-01-02T10:00:00', '2024-01-01T10:00:00'"
    })
    @DisplayName("생성 실패 - 종료날짜가 시작날짜보다 이전")
    void create_fail_endDateBeforeStartDate(String testDescription, LocalDateTime startDate, LocalDateTime endDate) {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EventDateTime.of(startDate, endDate, false));
        assertThat(exception.getMessage()).isEqualTo("종료 날짜는 시작 날짜보다 과거일 수 없습니다.");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'시작시간이 자정이 아님', '2024-01-01T01:00:00', '2024-01-02T00:00:00'",
            "'종료시간이 자정이 아님', '2024-01-01T00:00:00', '2024-01-02T01:00:00'",
            "'둘 다 자정이 아님', '2024-01-01T01:00:00', '2024-01-02T01:00:00'"
    })
    @DisplayName("생성 실패 - 종일일정이지만 시간이 자정이 아님")
    void create_fail_allDayButNotMidnight(String testDescription, LocalDateTime startDate, LocalDateTime endDate) {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EventDateTime.of(startDate, endDate, true));
        assertThat(exception.getMessage()).isEqualTo("종일일정의 날짜가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("생성 실패 - 종일일정이지만 하루 미만 차이")
    void create_fail_allDayButLessThanOneDay() {
        // GIVEN
        LocalDateTime sameDay = LocalDateTime.of(2024, 1, 1, 0, 0);

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EventDateTime.of(sameDay, sameDay, true));
        assertThat(exception.getMessage()).isEqualTo("종일일정의 날짜가 올바르지 않습니다.");
    }

    // ================ 경계값 테스트 ================

    @Test
    @DisplayName("경계값 성공 - 시작날짜와 종료날짜 동일")
    void create_success_sameStartAndEndDate() {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.of(START_DATE, START_DATE, false);

        // THEN
        assertThat(eventDateTime.getStartDate()).isEqualTo(START_DATE);
        assertThat(eventDateTime.getEndDate()).isEqualTo(START_DATE);
        assertThat(eventDateTime.getIsAllDay()).isFalse();
    }

    @Test
    @DisplayName("경계값 성공 - 종일일정 정확히 하루 차이")
    void create_success_allDayExactlyOneDay() {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.of(MIDNIGHT_START, MIDNIGHT_END, true);

        // THEN
        assertThat(eventDateTime.getStartDate()).isEqualTo(MIDNIGHT_START);
        assertThat(eventDateTime.getEndDate()).isEqualTo(MIDNIGHT_END);
        assertThat(eventDateTime.getIsAllDay()).isTrue();
    }

    @Test
    @DisplayName("경계값 성공 - 종일일정 여러 날 차이")
    void create_success_allDayMultipleDays() {
        // GIVEN
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 5, 0, 0);

        // WHEN
        EventDateTime eventDateTime = EventDateTime.of(startDate, endDate, true);

        // THEN
        assertThat(eventDateTime.getStartDate()).isEqualTo(startDate);
        assertThat(eventDateTime.getEndDate()).isEqualTo(endDate);
        assertThat(eventDateTime.getIsAllDay()).isTrue();
    }

    // ================ fromJson 테스트 ================

    @Test
    @DisplayName("fromJson - 정상 생성")
    void fromJson_success() {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.fromJson(START_DATE, END_DATE, false);

        // THEN
        assertThat(eventDateTime.getStartDate()).isEqualTo(START_DATE);
        assertThat(eventDateTime.getEndDate()).isEqualTo(END_DATE);
        assertThat(eventDateTime.getIsAllDay()).isFalse();
    }

    @Test
    @DisplayName("fromJson - isAllDay null일 때 기본값 false")
    void fromJson_success_isAllDayNullDefaultFalse() {
        // WHEN
        EventDateTime eventDateTime = EventDateTime.fromJson(START_DATE, END_DATE, null);

        // THEN
        assertThat(eventDateTime.getIsAllDay()).isFalse();
    }
}