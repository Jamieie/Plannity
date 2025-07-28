package org.mi.plannitybe.exception;

public class EventListNotFoundException extends RuntimeException {
    public EventListNotFoundException(String message) {
        super(message);
    }

    public EventListNotFoundException() {
        super("존재하지 않는 Event List 입니다.");
    }
}
