package org.mi.plannitybe.exception;

public class ForbiddenEventListAccessException extends RuntimeException {
    public ForbiddenEventListAccessException(String message) {
        super(message);
    }

    public ForbiddenEventListAccessException() {
        super("접근 권한이 없는 Event List 입니다.");
    }
}
