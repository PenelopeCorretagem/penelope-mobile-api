package com.penelopec.penelopemobileapi.shared.security;

import java.util.List;
import java.util.Objects;

public record AuthenticatedUser(String email, List<String> roles) {

  public AuthenticatedUser {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("O e-mail do usuário autenticado é obrigatório.");
    }

    roles = roles == null ? List.of() : roles.stream()
      .filter(Objects::nonNull)
      .filter(role -> !role.isBlank())
      .distinct()
      .toList();
  }
}
