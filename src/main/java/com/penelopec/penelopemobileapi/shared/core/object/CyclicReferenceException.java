package com.penelopec.penelopemobileapi.shared.core.object;

/**
 * Lançada quando a cadeia de composição possui uma referência circular,
 * o que causaria um loop infinito de desempacotamento (unwrap).
 */
public class CyclicReferenceException extends RuntimeException {
  public CyclicReferenceException(String message) {
    super(message);
  }
}
