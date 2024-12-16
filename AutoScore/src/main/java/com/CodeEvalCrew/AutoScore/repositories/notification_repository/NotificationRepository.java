package com.CodeEvalCrew.AutoScore.repositories.notification_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
}
