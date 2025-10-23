package org.mi.plannitybe.exception;

public class TaskAccessDeniedException extends ResourceAccessDeniedException {
    public TaskAccessDeniedException(String userId, Long taskId) {
        super(String.format("사용자(userId=%s)가 할일(taskId=%s)에 접근할 권한이 없습니다.", userId, taskId),
                userId,
                taskId,
                "TASK");
    }
}