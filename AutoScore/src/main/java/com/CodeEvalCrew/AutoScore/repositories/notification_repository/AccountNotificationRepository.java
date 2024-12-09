package com.CodeEvalCrew.AutoScore.repositories.notification_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CodeEvalCrew.AutoScore.models.Entity.Accout_Notification;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Status_Enum;

public interface AccountNotificationRepository extends JpaRepository<Accout_Notification, Long> {
    // Method to find by account ID and status
    List<Accout_Notification> findByAccountAccountIdAndStatus(Long accountId, Notification_Status_Enum status);
    // Tìm AccountNotification dựa trên accountId và notificationId
    Optional<Accout_Notification> findByAccountAccountIdAndNotificationNotificationId(Long accountId, Long notificationId);
}
