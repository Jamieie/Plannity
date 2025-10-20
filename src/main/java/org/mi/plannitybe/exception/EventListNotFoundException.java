package org.mi.plannitybe.exception;

public class EventListNotFoundException extends ResourceNotFoundException {

    public EventListNotFoundException(String userId, Long eventListId) {
        super(String.format("사용자(userId=%s)가 일정리스트(eventListId=%s)룰 찾을 수 없습니다.", userId, eventListId),
                userId,
                eventListId,
                "EVENT_LIST");
    }
}
