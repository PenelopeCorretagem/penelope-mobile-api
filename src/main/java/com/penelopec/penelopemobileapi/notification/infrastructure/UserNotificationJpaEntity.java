package com.penelopec.penelopemobileapi.notification.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
  name = "usuario_notificacao",
  uniqueConstraints = @UniqueConstraint(name = "uq_usuario_notificacao", columnNames = {"usuario_id", "notificacao_id"})
)
@Getter
@NoArgsConstructor
public class UserNotificationJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notificacao_id", nullable = false)
  private NotificationJpaEntity notification;

  @Column(name = "lida_em")
  private Instant readAt;

  @Column(name = "excluida_em")
  private Instant deletedAt;
}
