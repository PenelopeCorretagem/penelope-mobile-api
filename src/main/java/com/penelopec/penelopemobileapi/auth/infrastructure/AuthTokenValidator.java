package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.shared.security.AuthenticatedUser;
import com.penelopec.penelopemobileapi.shared.security.TokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthTokenValidator implements TokenValidator {
  private final TokenService tokenService;

  @Override
  public Optional<AuthenticatedUser> validate(String token) {
    return tokenService.validate(token)
      .map(identity -> new AuthenticatedUser(
        identity.email(),
        List.of(identity.accessLevel().name())
      ));
  }
}
