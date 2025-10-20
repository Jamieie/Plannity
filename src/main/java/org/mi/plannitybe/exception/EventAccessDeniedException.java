package org.mi.plannitybe.exception;

public class EventAccessDeniedException extends ResourceAccessDeniedException {

    public EventAccessDeniedException(String userId, Long eventId) {
        super(String.format("사용자(userId=%s)가 일정(eventId=%s)에 접근할 권한이 없습니다.", userId, eventId),
                userId,
                eventId,
                "EVENT");
    }
}
