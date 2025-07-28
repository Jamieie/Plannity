package org.mi.plannitybe.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException() {
        super("할일이 존재하지 않습니다.");
    }

    public TaskNotFoundException(Long taskId) {
        super("할일이 존재하지 않습니다: " + taskId);
    }
}
