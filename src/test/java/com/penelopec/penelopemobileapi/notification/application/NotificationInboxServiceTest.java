package com.penelopec.penelopemobileapi.notification.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.notification.infrastructure.SpringUserNotificationRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationInboxServiceTest {

  private static final Instant CURRENT_TIME = Instant.parse("2026-09-11T14:00:00Z");

  private final UserRepository users = mock(UserRepository.class);
  private final SpringUserNotificationRepository notifications = mock(SpringUserNotificationRepository.class);
  private final NotificationInboxService service = new NotificationInboxService(
    users,
    notifications,
    Clock.fixed(CURRENT_TIME, ZoneOffset.UTC)
  );

  @Test
  void shouldMarkOnlyAuthenticatedUserNotificationAsRead() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(notifications.markAsRead(1L, 10L, CURRENT_TIME)).thenReturn(1L);

    service.markAsRead("ana@penelope.com", 10L);

    verify(notifications).markAsRead(1L, 10L, CURRENT_TIME);
  }

  @Test
  void shouldSoftDeleteOnlyAuthenticatedUserNotification() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(notifications.markAsDeleted(1L, 10L, CURRENT_TIME)).thenReturn(1L);

    service.delete("ana@penelope.com", 10L);

    verify(notifications).markAsDeleted(1L, 10L, CURRENT_TIME);
  }

  @Test
  void shouldHideNotificationNotOwnedByAuthenticatedUser() {
    when(users.findByEmail("ana@penelope.com")).thenReturn(Optional.of(user()));
    when(notifications.markAsRead(1L, 10L, CURRENT_TIME)).thenReturn(0L);

    assertThatThrownBy(() -> service.markAsRead("ana@penelope.com", 10L))
      .isInstanceOf(NotFoundException.class)
      .hasMessage("Notificação não encontrada.");
  }

  private User user() {
    return User.restore(1L, "Ana", "ana@penelope.com", null, "password", AccessLevel.CLIENTE, null, null);
  }
}