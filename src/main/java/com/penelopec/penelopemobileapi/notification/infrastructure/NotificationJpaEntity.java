package com.penelopec.penelopemobileapi.notification.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "notificacao")
@Getter
@NoArgsConstructor
public class NotificationJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titulo", nullable = false)
  private String title;

  @Column(name = "mensagem", nullable = false)
  private String message;

  @Column(name = "criado_em", nullable = false)
  private Instant createdAt;
}