package com.penelopec.penelopemobileapi.shared.core.exception;

public class RetryExhaustedException extends RuntimeException {
  public RetryExhaustedException(String message, Throwable cause) {
    super(message, cause);
  }
}