package com.penelopec.penelopemobileapi.shared.core.exception;

public class AsyncExecutionException extends RuntimeException {
  public AsyncExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}