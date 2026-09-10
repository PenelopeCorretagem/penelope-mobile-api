package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {
  private final SpringUserRepository repository;

  @Override
  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<User> findByPasswordResetToken(String token) {
    return repository.findByPasswordResetToken(token).map(this::toDomain);
  }

  @Override
  public User save(User user) {
    return toDomain(repository.save(toEntity(user)));
  }

  private User toDomain(UserJpaEntity entity) {
    return User.restore(
      entity.getId(),
      entity.getEmail(),
      entity.getPassword(),
      entity.getAccessLevel(),
      entity.getPasswordResetToken(),
      entity.getPasswordResetTokenExpiry()
    );
  }

  private UserJpaEntity toEntity(User user) {
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(user.getId());
    entity.setEmail(user.getEmail());
    entity.setPassword(user.getPassword());
    entity.setAccessLevel(user.getAccessLevel());
    entity.setPasswordResetToken(user.getPasswordResetToken());
    entity.setPasswordResetTokenExpiry(user.getPasswordResetTokenExpiry());
    return entity;
  }
}
