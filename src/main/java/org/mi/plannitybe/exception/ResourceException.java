package org.mi.plannitybe.exception;

import lombok.Getter;

@Getter
public abstract class ResourceException extends RuntimeException {

    private final String userId;         // 요청한 사용자 ID
    private final Object resourceId;     // 접근 시도한 리소스 ID
    private final String resourceType;   // 접근 시도한 리소스 종류

    protected ResourceException(String message, String userId, Object resourceId, String resourceType) {
        super(message);
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
}
