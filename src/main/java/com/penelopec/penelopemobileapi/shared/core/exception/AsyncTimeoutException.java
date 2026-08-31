package com.penelopec.penelopemobileapi.shared.core.exception;

public class AsyncTimeoutException extends RuntimeException {
  public AsyncTimeoutException(String message) {
    super(message);
  }
}