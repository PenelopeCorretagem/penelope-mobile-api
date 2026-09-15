package com.penelopec.penelopemobileapi.auth.domain;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findByEmail(String email);
  Optional<User> findByPasswordResetToken(String token);
  User save(User user);
}