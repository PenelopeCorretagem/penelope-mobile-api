package com.penelopec.penelopemobileapi.notification.application;

import com.penelopec.penelopemobileapi.auth.domain.AuthErrorCode;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.notification.infrastructure.SpringUserNotificationRepository;
import com.penelopec.penelopemobileapi.notification.infrastructure.UserNotificationJpaEntity;
import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationInboxService {
  private final UserRepository users;
  private final SpringUserNotificationRepository notifications;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<UserNotificationResponse> findInbox(String email) {
    return notifications.findInboxByUserId(findUser(email).getId()).stream()
      .map(this::toResponse)
      .toList();
  }

  @Transactional
  public void markAsRead(String email, Long notificationId) {
    long updated = notifications.markAsRead(findUser(email).getId(), notificationId, clock.instant());
    ensureNotificationWasUpdated(updated);
  }

  @Transactional
  public void delete(String email, Long notificationId) {
    long updated = notifications.markAsDeleted(findUser(email).getId(), notificationId, clock.instant());
    ensureNotificationWasUpdated(updated);
  }

  private User findUser(String email) {
    return users.findByEmail(email)
      .orElseThrow(() -> new NotFoundException(AuthErrorCode.USER_NOT_FOUND.toError()));
  }

  private void ensureNotificationWasUpdated(long updated) {
    if (updated == 0) {
      throw new NotFoundException(DomainError.of("NOTIFICATION_404", "Notificação não encontrada."));
    }
  }

  private UserNotificationResponse toResponse(UserNotificationJpaEntity userNotification) {
    var notification = userNotification.getNotification();
    return new UserNotificationResponse(
      userNotification.getId(),
      notification.getId(),
      notification.getTitle(),
      notification.getMessage(),
      notification.getCreatedAt(),
      userNotification.getReadAt()
    );
  }
}