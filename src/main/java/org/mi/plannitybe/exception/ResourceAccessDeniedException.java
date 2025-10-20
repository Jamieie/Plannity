package org.mi.plannitybe.exception;

public abstract class ResourceAccessDeniedException extends ResourceException {
  protected ResourceAccessDeniedException(String message, String userId, Object resourceId, String resourceType) {
    super(message, userId, resourceId, resourceType);
  }
}
