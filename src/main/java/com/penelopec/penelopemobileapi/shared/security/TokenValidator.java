package com.penelopec.penelopemobileapi.shared.security;

import java.util.Optional;

@FunctionalInterface
public interface TokenValidator {

  Optional<AuthenticatedUser> validate(String token);
}
