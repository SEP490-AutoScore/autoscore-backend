package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class FileProcessingProgressService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private int lastSentProgress = -1;

    // Đăng ký một emitter mới
    public void registerEmitter(String clientId, SseEmitter emitter) {
        emitters.put(clientId, emitter);
        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(e -> emitters.remove(clientId));
    }

    public void sendProgress(int progress) {
        if (progress != lastSentProgress) { // Gửi nếu tiến trình thay đổi
            lastSentProgress = progress;
            emitters.forEach((clientId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(progress)
                            .reconnectTime(3000));
                } catch (IOException e) {
                    emitters.remove(clientId);
                }
            });
        }
    }
}
