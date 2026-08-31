package com.penelopec.penelopemobileapi.shared.core.validation;

import java.util.List;

/**
 * Representa as violações encontradas durante uma validação.
 */
public record Violations(List<String> messages) {
  public Violations {
    messages = List.copyOf(messages); // Imutabilidade profunda da lista
  }

  public static Violations empty() {
    return new Violations(List.of());
  }

  public static Violations of(String message) {
    return new Violations(List.of(message));
  }
}
