package com.penelopec.penelopemobileapi.favorite.application;

import com.penelopec.penelopemobileapi.auth.domain.AuthErrorCode;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import com.penelopec.penelopemobileapi.favorite.infrastructure.FavoriteJpaEntity;
import com.penelopec.penelopemobileapi.favorite.infrastructure.SpringFavoriteRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
  private final UserRepository users;
  private final SpringAdvertisementRepository advertisements;
  private final SpringFavoriteRepository favorites;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<FavoriteResponse> findAll(String email) {
    User user = findUser(email);
    return favorites.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
      .map(favorite -> new FavoriteResponse(favorite.getAdvertisementId(), favorite.getCreatedAt()))
      .toList();
  }

  @Transactional
  public void add(String email, Long advertisementId) {
    User user = findUser(email);
    ensureActiveAdvertisement(advertisementId);

    if (favorites.existsByUserIdAndAdvertisementId(user.getId(), advertisementId)) {
      return;
    }

    FavoriteJpaEntity favorite = new FavoriteJpaEntity();
    favorite.setUserId(user.getId());
    favorite.setAdvertisementId(advertisementId);
    favorite.setCreatedAt(clock.instant());
    favorites.save(favorite);
  }

  @Transactional
  public void remove(String email, Long advertisementId) {
    favorites.deleteByUserIdAndAdvertisementId(findUser(email).getId(), advertisementId);
  }

  private User findUser(String email) {
    return users.findByEmail(email)
      .orElseThrow(() -> new NotFoundException(AuthErrorCode.USER_NOT_FOUND.toError()));
  }

  private void ensureActiveAdvertisement(Long advertisementId) {
    if (!advertisements.existsByIdAndActiveTrue(advertisementId)) {
      throw new NotFoundException(DomainError.of("CATALOG_404", "Anúncio não encontrado."));
    }
  }
}