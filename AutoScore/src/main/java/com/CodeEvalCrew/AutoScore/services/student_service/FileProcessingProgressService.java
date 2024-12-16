package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class FileProcessingProgressService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private int lastProgress = -1; // Trạng thái tiến trình cuối cùng

    public void registerEmitter(SseEmitter emitter, AtomicInteger totalTasks, AtomicInteger completedTasks, AtomicInteger failedTasks) {
        emitters.add(emitter);

        // Xóa emitter khỏi danh sách khi hoàn thành hoặc timeout
        emitter.onCompletion(() -> {
            if (emitters.contains(emitter)) {
                emitters.remove(emitter);
            }
        });
        emitter.onTimeout(() -> {
            if (emitters.contains(emitter)) {
                emitters.remove(emitter);
            }
        });
        emitter.onError((e) -> {
            if (emitters.contains(emitter)) {
                emitters.remove(emitter);
            }
        });

        // Gửi heartbeat định kỳ để giữ kết nối mở
        new Thread(() -> {
            while (emitters.contains(emitter)) {
                try {
                    Thread.sleep(1_000L); // Gửi heartbeat mỗi 5 giây
                    emitter.send(SseEmitter.event().data("keep-alive"));
                } catch (IOException | InterruptedException e) {
                    emitters.remove(emitter);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    public void sendProgress(int progress) {
        if (progress != lastProgress) { // Chỉ gửi khi tiến trình thay đổi
            lastProgress = progress;
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().data(progress));
                } catch (IllegalStateException | IOException e) {
                    emitters.remove(emitter);
                }
            }
        }
    }
}
