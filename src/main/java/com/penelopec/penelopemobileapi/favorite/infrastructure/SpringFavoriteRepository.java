package com.penelopec.penelopemobileapi.favorite.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringFavoriteRepository extends JpaRepository<FavoriteJpaEntity, Long> {
  List<FavoriteJpaEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
  boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);
  long deleteByUserIdAndAdvertisementId(Long userId, Long advertisementId);
}