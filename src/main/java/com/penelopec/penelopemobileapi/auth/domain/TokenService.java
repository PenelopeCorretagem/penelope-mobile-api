package com.penelopec.penelopemobileapi.auth.domain;

import java.util.Optional;

public interface TokenService {
  String generate(String email, AccessLevel accessLevel);
  Optional<TokenIdentity> validate(String token);
}