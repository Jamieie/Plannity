package org.mi.plannitybe.exception;

public abstract class ResourceNotFoundException extends ResourceException {
    protected ResourceNotFoundException(String message, String userId, Object resourceId, String resourceType) {
        super(message, userId, resourceId, resourceType);
    }
}
