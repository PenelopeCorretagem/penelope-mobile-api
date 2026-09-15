package com.penelopec.penelopemobileapi.favorite.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import com.penelopec.penelopemobileapi.favorite.infrastructure.FavoriteJpaEntity;
import com.penelopec.penelopemobileapi.favorite.infrastructure.SpringFavoriteRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoriteServiceTest {

  private final UserRepository users = mock(UserRepository.class);
  private final SpringAdvertisementRepository advertisements = mock(SpringAdvertisementRepository.class);
  private final SpringFavoriteRepository favorites = mock(SpringFavoriteRepository.class);
  private final FavoriteService service = new FavoriteService(
    users,
    advertisements,
    favorites,
    Clock.fixed(Instant.parse("2026-09-11T12:00:00Z"), ZoneOffset.UTC)
  );

  @Test
  void shouldPersistFavoriteForAuthenticatedUser() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(advertisements.existsByIdAndActiveTrue(10L)).thenReturn(true);
    when(favorites.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(false);

    service.add("ana@penelope.com", 10L);

    ArgumentCaptor<FavoriteJpaEntity> favoriteCaptor = ArgumentCaptor.forClass(FavoriteJpaEntity.class);
    verify(favorites).save(favoriteCaptor.capture());
    assertThat(favoriteCaptor.getValue().getUserId()).isEqualTo(1L);
    assertThat(favoriteCaptor.getValue().getAdvertisementId()).isEqualTo(10L);
    assertThat(favoriteCaptor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-09-11T12:00:00Z"));
  }

  @Test
  void shouldNotPersistDuplicateFavorite() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(advertisements.existsByIdAndActiveTrue(10L)).thenReturn(true);
    when(favorites.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(true);

    service.add("ana@penelope.com", 10L);

    verify(favorites, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void shouldRejectInactiveOrMissingAdvertisement() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(advertisements.existsByIdAndActiveTrue(10L)).thenReturn(false);

    assertThatThrownBy(() -> service.add("ana@penelope.com", 10L))
      .isInstanceOf(NotFoundException.class)
      .hasMessage("Anúncio não encontrado.");

    verify(favorites, never()).save(org.mockito.ArgumentMatchers.any());
  }

  private User user() {
    return User.restore(1L, "Ana", "ana@penelope.com", null, "password", AccessLevel.CLIENTE, null, null);
  }
}