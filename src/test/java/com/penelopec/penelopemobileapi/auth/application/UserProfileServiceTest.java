package com.penelopec.penelopemobileapi.auth.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.TokenIdentity;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileServiceTest {

  @Test
  void shouldUpdateProfileAndGenerateTokenWhenEmailChanges() {
    InMemoryUserRepository users = new InMemoryUserRepository();
    users.save(user(1L, "Ana", "ana@penelope.com"));
    UserProfileService service = new UserProfileService(users, new FixedTokenService());

    UpdateUserProfileResponse response = service.updateProfile(
      "ana@penelope.com",
      new UpdateUserProfileRequest("Ana Silva", "ana.silva@penelope.com", LocalDate.of(1995, 4, 12))
    );

    assertThat(response.profile()).isEqualTo(
      new UserProfileResponse("Ana Silva", "ana.silva@penelope.com", LocalDate.of(1995, 4, 12))
    );
    assertThat(response.token()).isEqualTo("renewed-token");
    assertThat(users.findByEmail("ana.silva@penelope.com")).isPresent();
  }

  @Test
  void shouldRejectProfileUpdateWhenEmailBelongsToAnotherUser() {
    InMemoryUserRepository users = new InMemoryUserRepository();
    users.save(user(1L, "Ana", "ana@penelope.com"));
    users.save(user(2L, "Bruno", "bruno@penelope.com"));
    UserProfileService service = new UserProfileService(users, new FixedTokenService());

    assertThatThrownBy(() -> service.updateProfile(
      "ana@penelope.com",
      new UpdateUserProfileRequest("Ana", "bruno@penelope.com", null)
    )).isInstanceOf(ValidationException.class)
      .hasMessage("O e-mail informado já está em uso.");
  }

  private static User user(Long id, String name, String email) {
    return User.restore(id, name, email, null, "encoded-password", AccessLevel.CLIENTE, null, (Instant) null);
  }

  private static final class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findByEmail(String email) {
      return Optional.ofNullable(users.get(email));
    }

    @Override
    public Optional<User> findByPasswordResetToken(String token) {
      return users.values().stream()
        .filter(user -> token.equals(user.getPasswordResetToken()))
        .findFirst();
    }

    @Override
    public User save(User user) {
      users.entrySet().removeIf(entry -> entry.getValue().getId().equals(user.getId()));
      users.put(user.getEmail(), user);
      return user;
    }
  }

  private static final class FixedTokenService implements TokenService {
    @Override
    public String generate(String email, AccessLevel accessLevel) {
      return "renewed-token";
    }

    @Override
    public Optional<TokenIdentity> validate(String token) {
      return Optional.empty();
    }
  }
}