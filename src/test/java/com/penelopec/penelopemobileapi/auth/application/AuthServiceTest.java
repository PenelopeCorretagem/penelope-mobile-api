package com.penelopec.penelopemobileapi.auth.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.PasswordEncoder;
import com.penelopec.penelopemobileapi.auth.domain.TokenIdentity;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.BusinessException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

  private final InMemoryUserRepository users = new InMemoryUserRepository();
  private final AuthService service = new AuthService(users, new TestPasswordEncoder(), new FixedTokenService(),
    (email, token) -> { }, Clock.fixed(Instant.parse("2026-09-10T00:00:00Z"), ZoneOffset.UTC));

  @Test
  void shouldAuthenticateUserWithValidCredentials() {
    users.save(User.restore(1L, "user@penelope.com", "encoded:secret", AccessLevel.ADMINISTRADOR, null, null));

    LoginResponse response = service.login(new LoginRequest("user@penelope.com", "secret"));

    assertThat(response.token()).isEqualTo("token");
    assertThat(response.accessLevel()).isEqualTo("ADMINISTRADOR");
  }

  @Test
  void shouldRegisterClientWithEncodedPassword() {
    RegisterResponse response = service.register(new RegisterRequest(
      "New User", LocalDate.parse("1990-05-12"), "new@penelope.com", "secret"
    ));

    User saved = users.findByEmail("new@penelope.com").orElseThrow();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.name()).isEqualTo("New User");
    assertThat(response.birthDate()).isEqualTo(LocalDate.parse("1990-05-12"));
    assertThat(response.accessLevel()).isEqualTo("CLIENTE");
    assertThat(saved.getPassword()).isEqualTo("encoded:secret");
  }

  @Test
  void shouldRejectRegistrationWithExistingEmail() {
    users.save(User.restore(1L, "user@penelope.com", "secret", AccessLevel.CLIENTE, null, null));

    assertThatThrownBy(() -> service.register(new RegisterRequest(
      "New User", LocalDate.parse("1990-05-12"), "user@penelope.com", "secret"
    )))
      .isInstanceOf(ValidationException.class)
      .hasMessage("O e-mail informado já está em uso.");
  }

  @Test
  void shouldRejectInvalidCredentials() {
    users.save(User.restore(1L, "user@penelope.com", "encoded:secret", AccessLevel.CLIENTE, null, null));

    assertThatThrownBy(() -> service.login(new LoginRequest("user@penelope.com", "wrong")))
      .isInstanceOf(BusinessException.class)
      .hasMessage("Usuário ou senha inválidos.");
  }

  @Test
  void shouldResetPasswordAndClearResetToken() {
    User user = User.restore(1L, "user@penelope.com", "encoded:old", AccessLevel.CLIENTE, "123456",
      Instant.parse("2026-09-10T01:00:00Z"));
    users.save(user);

    service.resetPassword("123456", "new");

    User saved = users.findByEmail("user@penelope.com").orElseThrow();
    assertThat(saved.getPassword()).isEqualTo("encoded:new");
    assertThat(saved.getPasswordResetToken()).isNull();
  }

  private static final class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> byEmail = new HashMap<>();
    public Optional<User> findByEmail(String email) { return Optional.ofNullable(byEmail.get(email)); }
    public Optional<User> findByPasswordResetToken(String token) { return byEmail.values().stream().filter(user -> token.equals(user.getPasswordResetToken())).findFirst(); }
    public User save(User user) {
      User persisted = user.getId() == null
        ? User.restore((long) byEmail.size() + 1, user.getName(), user.getEmail(), user.getBirthDate(),
          user.getPassword(), user.getAccessLevel(), user.getPasswordResetToken(), user.getPasswordResetTokenExpiry())
        : user;
      byEmail.put(persisted.getEmail(), persisted);
      return persisted;
    }
  }

  private static final class TestPasswordEncoder implements PasswordEncoder {
    public String encode(String rawPassword) { return "encoded:" + rawPassword; }
    public boolean matches(String rawPassword, String encryptedPassword) {
      return encode(rawPassword).equals(encryptedPassword);
    }
  }

  private static final class FixedTokenService implements TokenService {
    public String generate(String email, AccessLevel accessLevel) { return "token"; }
    public Optional<TokenIdentity> validate(String token) { return Optional.empty(); }
  }
}
