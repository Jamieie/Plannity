package org.mi.plannitybe.exception;

public class EventListAccessDeniedException extends ResourceAccessDeniedException {

    public EventListAccessDeniedException(String userId, Long eventListId) {
        super(String.format("사용자(userId=%s)가 일정리스트(eventListId=%s)에 접근할 권한이 없습니다.", userId, eventListId),
                userId,
                eventListId,
                "EVENT_LIST");
    }
}
