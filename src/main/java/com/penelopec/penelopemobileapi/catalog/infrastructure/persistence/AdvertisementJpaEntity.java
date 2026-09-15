package com.penelopec.penelopemobileapi.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "anuncio")
@Getter
@NoArgsConstructor
public class AdvertisementJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "empreendimento_id", nullable = false)
  private EstateJpaEntity estate;

  @Column(name = "preco", nullable = false)
  private BigDecimal price;

  @Column(name = "ativo", nullable = false)
  private boolean active;

  @Column(name = "destaque", nullable = false)
  private boolean featured;

  @Column(name = "criado_em", nullable = false)
  private LocalDateTime createdAt;
}