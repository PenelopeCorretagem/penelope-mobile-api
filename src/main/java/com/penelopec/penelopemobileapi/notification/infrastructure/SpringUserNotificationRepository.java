package com.penelopec.penelopemobileapi.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringUserNotificationRepository extends JpaRepository<UserNotificationJpaEntity, Long> {

  @Query("""
    select userNotification from UserNotificationJpaEntity userNotification
    join fetch userNotification.notification
    where userNotification.userId = :userId
      and userNotification.deletedAt is null
    order by userNotification.notification.createdAt desc
    """)
  List<UserNotificationJpaEntity> findInboxByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("""
    update UserNotificationJpaEntity userNotification
    set userNotification.readAt = coalesce(userNotification.readAt, :readAt)
    where userNotification.id = :notificationId
      and userNotification.userId = :userId
      and userNotification.deletedAt is null
    """)
  long markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId, @Param("readAt") Instant readAt);

  @Modifying
  @Query("""
    update UserNotificationJpaEntity userNotification
    set userNotification.deletedAt = coalesce(userNotification.deletedAt, :deletedAt)
    where userNotification.id = :notificationId
      and userNotification.userId = :userId
    """)
  long markAsDeleted(@Param("userId") Long userId, @Param("notificationId") Long notificationId, @Param("deletedAt") Instant deletedAt);
}