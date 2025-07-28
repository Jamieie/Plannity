package org.mi.plannitybe.exception;

public class InvalidAllDayEventDateException extends RuntimeException {
    public InvalidAllDayEventDateException(String message) {
        super(message);
    }

    public InvalidAllDayEventDateException() {
        super("종일일정의 시작날짜와 종료날짜가 유효하지 않습니다.");
    }
}
