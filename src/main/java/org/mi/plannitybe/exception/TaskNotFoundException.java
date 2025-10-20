package org.mi.plannitybe.exception;

public class TaskNotFoundException extends ResourceNotFoundException {

    public TaskNotFoundException(String userId, Long taskId) {
        super(String.format("사용자(userId=%s)가 할일(taskId=%s)을 찾을 수 없습니다.", userId, taskId),
                userId,
                taskId,
                "TASK");
    }
}
