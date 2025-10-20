package org.mi.plannitybe.exception;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(String userId, Long eventId) {
        super(String.format("사용자(userId=%s)가 일정(eventId=%s)을 찾을 수 없습니다.", userId, eventId),
                userId,
                eventId,
                "EVENT");
    }
}
