package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.AuthErrorCode;
import com.penelopec.penelopemobileapi.auth.domain.TokenIdentity;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.shared.core.exception.InfrastructureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class JwtTokenService implements TokenService {
  private static final String ISSUER = "Penelope-API";
  private final String secret;

  public JwtTokenService(@Value("${app.security.token.secret}") String secret) {
    this.secret = secret;
  }

  @Override
  public String generate(String email, AccessLevel accessLevel) {
    return JWT.create()
      .withIssuer(ISSUER)
      .withSubject(email)
      .withClaim("accessLevel", accessLevel.name())
      .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
      .sign(algorithm());
  }

  @Override
  public Optional<TokenIdentity> validate(String token) {
    try {
      var decoded = JWT.require(algorithm())
        .withIssuer(ISSUER)
        .build()
        .verify(token);

      String accessLevel = decoded.getClaim("accessLevel").asString();
      if (decoded.getSubject() == null || accessLevel == null) {
        return Optional.empty();
      }

      return Optional.of(new TokenIdentity(decoded.getSubject(), AccessLevel.valueOf(accessLevel)));
    } catch (JWTVerificationException | IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  private Algorithm algorithm() {
    if (secret == null || secret.isBlank()) {
      throw new InfrastructureException(AuthErrorCode.TOKEN_CONFIGURATION.defaultMessage());
    }
    return Algorithm.HMAC256(secret);
  }
}
