package com.CodeEvalCrew.AutoScore.services.notification_service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.NotificationView;
import com.CodeEvalCrew.AutoScore.models.Entity.Accout_Notification;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Notification;
import com.CodeEvalCrew.AutoScore.repositories.notification_repository.AccountNotificationRepository;
import com.CodeEvalCrew.AutoScore.repositories.notification_repository.NotificationRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificactionRepository;
    @Autowired
    private AccountNotificationRepository accountNotificationRepository;

    // Danh sách lưu các kết nối client
    private final CopyOnWriteArrayList<SseEmitter> clients = new CopyOnWriteArrayList<>();

    @Override
    public List<NotificationView> getUnReadNotification() throws Exception {
        List<NotificationView> result = new ArrayList<>();
        try {
            //get cur acc
            Long accountId = Util.getAuthenticatedAccountId();
            // get list unread not of ths account
            List<Accout_Notification> accountNotifications = accountNotificationRepository.findByAccountAccountIdAndStatus(accountId, Notification_Status_Enum.UNREAD);
            if (accountNotifications.isEmpty()) {
                throw new NoSuchElementException("Notification not found");
            }
            for (Accout_Notification accout_Notification : accountNotifications) {
                result.add(new NotificationView(accout_Notification.getNotification().getNotificationId(),
                        accout_Notification.getNotification().getTitle(),
                        accout_Notification.getNotification().getContent(),
                        accout_Notification.getNotification().getTargetUrl(),
                        accout_Notification.getNotification().getType()));
            }
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        }
    }

    @Override
    public NotificationView setReadNotification(Long notificationId) throws Exception {
        NotificationView result = new NotificationView();
        try {
            //get cur acc
            Long accountId = Util.getAuthenticatedAccountId();
            //checkNoti
            // checkEntityExistence(notificactionRepository.findById(notificationId),:"Notification", notificationId);
            Notification noti = checkEntityExistence(notificactionRepository.findById(notificationId), "Notification", notificationId);
            Optional<Accout_Notification> optAccoutNotification = accountNotificationRepository.findByAccountAccountIdAndNotificationNotificationId(accountId, notificationId);
            if (!optAccoutNotification.isPresent()) {

            }
            Accout_Notification accountNoti = optAccoutNotification.get();
            accountNoti.setStatus(Notification_Status_Enum.READED);
            accountNotificationRepository.save(accountNoti);

            result.setNotificationId(notificationId);
            result.setTargetUrl(noti.getTargetUrl());
            result.setTitle(noti.getTitle());
            result.setContent(noti.getContent());
            result.setTargetUrl(noti.getTargetUrl());
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws Exception {
        return entity.orElseThrow(() -> new NoSuchElementException(entityName + " id: " + entityId + " not found"));
    }

    // Đăng ký client và trả về một SSE emitter
    @Override
    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(0L); // Không timeout
        clients.add(emitter);

        // Xóa kết nối khi hoàn thành hoặc timeout
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError((e) -> clients.remove(emitter));

        return emitter;
    }

    // Gửi thông báo đến tất cả client
    @Override
    public void broadcastNotification(NotificationView notification) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification") // Tên sự kiện
                        .data(notification)); // Dữ liệu gửi
            } catch (IOException e) {
                clients.remove(emitter); // Xóa kết nối bị lỗi
            }
        }
    }
}
