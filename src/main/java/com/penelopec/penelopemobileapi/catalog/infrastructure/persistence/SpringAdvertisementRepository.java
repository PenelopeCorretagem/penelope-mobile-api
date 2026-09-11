package com.penelopec.penelopemobileapi.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringAdvertisementRepository extends JpaRepository<AdvertisementJpaEntity, Long> {

  @EntityGraph(attributePaths = {"estate", "estate.address", "estate.media", "estate.media.type", "estate.amenities"})
  @Query("""
    select distinct advertisement from AdvertisementJpaEntity advertisement
    join advertisement.estate estate
    where advertisement.active = :active
      and (:type is null or estate.type = :type)
    order by advertisement.createdAt desc
    """)
  List<AdvertisementJpaEntity> findCatalog(@Param("active") boolean active, @Param("type") EstateType type);

  @EntityGraph(attributePaths = {"estate", "estate.address", "estate.media", "estate.media.type", "estate.amenities"})
  Optional<AdvertisementJpaEntity> findByIdAndActiveTrue(Long id);
}