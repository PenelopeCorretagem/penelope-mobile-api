package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "senha", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "nivel_acesso")
  private AccessLevel accessLevel;

  @Column(name = "token_redefinicao_senha")
  private String passwordResetToken;

  @Column(name = "data_expiracao_token")
  private Instant passwordResetTokenExpiry;

}
