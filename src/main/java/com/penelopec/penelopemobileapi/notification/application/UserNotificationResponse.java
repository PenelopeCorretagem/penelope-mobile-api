package com.penelopec.penelopemobileapi.notification.application;

import java.time.Instant;

public record UserNotificationResponse(
  Long userNotificationId,
  Long id,
  String title,
  String message,
  Instant createdAt,
  Instant readAt
) {
}