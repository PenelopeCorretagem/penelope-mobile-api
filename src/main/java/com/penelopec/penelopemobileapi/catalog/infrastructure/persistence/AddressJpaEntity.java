package com.penelopec.penelopemobileapi.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "endereco")
@Getter
@NoArgsConstructor
public class AddressJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cidade", nullable = false)
  private String city;

  @Column(name = "regiao")
  private String region;

    @Column(name = "uf", nullable = false, columnDefinition = "CHAR(2)")
    private String state;

  private Double latitude;
  private Double longitude;
}
