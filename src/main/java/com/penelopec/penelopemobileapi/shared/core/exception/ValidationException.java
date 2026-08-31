package com.penelopec.penelopemobileapi.shared.core.exception;

public final class ValidationException extends CoreException {
  public ValidationException(DomainError domainError) {
    super(domainError);
  }

  @Override
  public boolean isRetryable() {
    return true;
  }
}
