package com.CodeEvalCrew.AutoScore.utils;

import org.springframework.stereotype.Component;

@Component
public class UploadProgressListener {
    private long bytesRead = 0;
    private long contentLength = 0;

    public synchronized void updateProgress(long bytesRead, long contentLength) {
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
    }

    public synchronized int getPercentComplete() {
        return contentLength > 0 ? (int) ((bytesRead * 100) / contentLength) : 0;
    }
}

