package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.NotificationView;
import com.CodeEvalCrew.AutoScore.services.notification_service.INotificationService;


@RestController
@RequestMapping("api/notifications")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<?> getUnReadNotification() {
        List<NotificationView> result;
        try {
            result = notificationService.getUnReadNotification();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> setReadNotification(@RequestBody Long notificationId) {
        NotificationView result;
        try {
            result = notificationService.setReadNotification(notificationId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // API để client đăng ký nhận SSE
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return notificationService.registerClient();
    }

    // API để server gửi thông báo đến tất cả client
    @PostMapping("/broadcast")
    public void broadcastNotification(@RequestBody NotificationView notification) {
        notificationService.broadcastNotification(notification);
    }

}
