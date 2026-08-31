package com.penelopec.penelopemobileapi.shared.core.validation;

import java.util.Objects;

/**
 * Encapsula o resultado de uma operação de validação.
 */
public record ValidationResult(Violations violations) {
  public ValidationResult {
    Objects.requireNonNull(violations, "Violations não pode ser nulo");
  }

  public boolean isValid() {
    return violations().messages().isEmpty();
  }

  public static ValidationResult valid() {
    return new ValidationResult(Violations.empty());
  }

  public static ValidationResult invalid(String message) {
    return new ValidationResult(Violations.of(message));
  }
}
