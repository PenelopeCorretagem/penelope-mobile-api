package com.penelopec.penelopemobileapi.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface SpringUserRepository extends JpaRepository<UserJpaEntity, Long> {
  Optional<UserJpaEntity> findByEmail(String email);
  Optional<UserJpaEntity> findByPasswordResetToken(String token);
}