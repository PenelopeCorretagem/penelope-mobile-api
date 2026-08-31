package com.penelopec.penelopemobileapi.shared.core.exception;

public final class BusinessException extends CoreException {
  public BusinessException(DomainError domainError) {
    super(domainError);
  }

  @Override
  public boolean isRetryable() {
    return false;
  }
}
