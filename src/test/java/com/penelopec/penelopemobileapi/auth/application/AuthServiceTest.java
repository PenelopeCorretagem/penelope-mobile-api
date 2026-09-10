package com.penelopec.penelopemobileapi.auth.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.PasswordEncoder;
import com.penelopec.penelopemobileapi.auth.domain.TokenIdentity;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

  private final InMemoryUserRepository users = new InMemoryUserRepository();
  private final AuthService service = new AuthService(users, new PlainPasswordEncoder(), new FixedTokenService(),
    (email, token) -> { }, Clock.fixed(Instant.parse("2026-09-10T00:00:00Z"), ZoneOffset.UTC));

  @Test
  void shouldAuthenticateUserWithValidCredentials() {
    users.save(User.restore(1L, "user@penelope.com", "secret", AccessLevel.ADMINISTRADOR, null, null));

    LoginResponse response = service.login(new LoginRequest("user@penelope.com", "secret"));

    assertThat(response.token()).isEqualTo("token");
    assertThat(response.accessLevel()).isEqualTo("ADMINISTRADOR");
  }

  @Test
  void shouldRejectInvalidCredentials() {
    users.save(User.restore(1L, "user@penelope.com", "secret", AccessLevel.CLIENTE, null, null));

    assertThatThrownBy(() -> service.login(new LoginRequest("user@penelope.com", "wrong")))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Usuário ou senha inválidos.");
  }

  @Test
  void shouldResetPasswordAndClearResetToken() {
    User user = User.restore(1L, "user@penelope.com", "old", AccessLevel.CLIENTE, "123456",
      Instant.parse("2026-09-10T01:00:00Z"));
    users.save(user);

    service.resetPassword("123456", "new");

    User saved = users.findByEmail("user@penelope.com").orElseThrow();
    assertThat(saved.getPassword()).isEqualTo("new");
    assertThat(saved.getPasswordResetToken()).isNull();
  }

  private static final class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> byEmail = new HashMap<>();
    public Optional<User> findByEmail(String email) { return Optional.ofNullable(byEmail.get(email)); }
    public Optional<User> findByPasswordResetToken(String token) { return byEmail.values().stream().filter(user -> token.equals(user.getPasswordResetToken())).findFirst(); }
    public User save(User user) { byEmail.put(user.getEmail(), user); return user; }
  }

  private static final class PlainPasswordEncoder implements PasswordEncoder {
    public String encode(String rawPassword) { return rawPassword; }
    public boolean matches(String rawPassword, String encryptedPassword) { return rawPassword.equals(encryptedPassword); }
  }

  private static final class FixedTokenService implements TokenService {
    public String generate(String email, AccessLevel accessLevel) { return "token"; }
    public Optional<TokenIdentity> validate(String token) { return Optional.empty(); }
  }
}
