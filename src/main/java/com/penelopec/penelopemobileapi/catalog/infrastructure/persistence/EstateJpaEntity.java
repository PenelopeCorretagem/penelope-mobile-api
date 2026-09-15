package com.penelopec.penelopemobileapi.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "empreendimento")
@Getter
@NoArgsConstructor
public class EstateJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titulo", nullable = false)
  private String title;

  @Column(name = "descricao", nullable = false)
  private String description;

  private Double area;

  @Column(name = "quantidade_quartos")
  private Integer numberOfRooms;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false)
  private EstateType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "endereco_id", nullable = false)
  private AddressJpaEntity address;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "empreendimento_id")
  private Set<EstateMediaJpaEntity> media = new LinkedHashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "empreendimento_comodidade",
    joinColumns = @JoinColumn(name = "empreendimento_id"),
    inverseJoinColumns = @JoinColumn(name = "comodidade_id")
  )
  private Set<AmenityJpaEntity> amenities = new LinkedHashSet<>();
}