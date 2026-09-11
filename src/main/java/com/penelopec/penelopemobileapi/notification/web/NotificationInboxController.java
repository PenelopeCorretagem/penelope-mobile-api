package com.penelopec.penelopemobileapi.notification.web;

import com.penelopec.penelopemobileapi.notification.application.NotificationInboxService;
import com.penelopec.penelopemobileapi.notification.application.UserNotificationResponse;
import com.penelopec.penelopemobileapi.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/users/me/notifications")
@RequiredArgsConstructor
public class NotificationInboxController {
  private final NotificationInboxService notificationInboxService;

  @GetMapping
  public ResponseEntity<List<UserNotificationResponse>> findInbox(@AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(notificationInboxService.findInbox(user.email()));
  }

  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<Void> markAsRead(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable("notificationId") Long notificationId) {
    notificationInboxService.markAsRead(user.email(), notificationId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable("notificationId") Long notificationId) {
    notificationInboxService.delete(user.email(), notificationId);
    return ResponseEntity.noContent().build();
  }
}