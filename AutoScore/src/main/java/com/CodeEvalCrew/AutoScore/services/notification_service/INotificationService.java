package com.CodeEvalCrew.AutoScore.services.notification_service;

import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.NotificationView;

public interface INotificationService {

    List<NotificationView> getUnReadNotification() throws Exception;

    NotificationView setReadNotification(Long notificationId) throws Exception;

    public SseEmitter registerClient();

    public void broadcastNotification(NotificationView notification);

}
