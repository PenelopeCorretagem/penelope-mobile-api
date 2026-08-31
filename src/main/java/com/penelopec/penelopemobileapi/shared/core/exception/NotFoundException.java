package com.penelopec.penelopemobileapi.shared.core.exception;

public final class NotFoundException extends CoreException {
  public NotFoundException(DomainError domainError) {
    super(domainError);
  }

  @Override
  public boolean isRetryable() {
    return false;
  }
}
