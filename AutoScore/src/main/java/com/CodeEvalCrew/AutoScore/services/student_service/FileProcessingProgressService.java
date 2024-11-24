package com.CodeEvalCrew.AutoScore.services.student_service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FileProcessingProgressService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void registerEmitter(SseEmitter emitter) {
        emitters.add(emitter);

        // Xóa emitter khỏi danh sách khi hoàn thành hoặc timeout
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // Gửi heartbeat định kỳ để giữ kết nối mở
        new Thread(() -> {
            while (emitters.contains(emitter)) {
                try {
                    Thread.sleep(15_000L); // 15 giây
                    emitter.send(SseEmitter.event().data("keep-alive"));
                } catch (IOException | InterruptedException e) {
                    emitters.remove(emitter); // Loại bỏ emitter nếu lỗi
                    break;
                }
            }
        }).start();
    }

    public void sendProgress(int progress) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(progress));
            } catch (IllegalStateException | IOException e) {
                // Xóa emitter khi không gửi được
                emitters.remove(emitter);
            }
        }
    }

}
