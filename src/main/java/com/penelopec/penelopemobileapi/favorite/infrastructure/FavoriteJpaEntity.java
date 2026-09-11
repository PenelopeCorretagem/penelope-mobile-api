package com.penelopec.penelopemobileapi.favorite.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
  name = "usuario_favorito",
  uniqueConstraints = @UniqueConstraint(name = "uq_usuario_favorito", columnNames = {"usuario_id", "anuncio_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class FavoriteJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false)
  private Long userId;

  @Column(name = "anuncio_id", nullable = false)
  private Long advertisementId;

  @Column(name = "criado_em", nullable = false)
  private Instant createdAt;
}