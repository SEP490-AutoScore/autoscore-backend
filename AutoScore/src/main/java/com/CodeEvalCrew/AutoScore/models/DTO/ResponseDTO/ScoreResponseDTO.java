package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponseDTO {

    private Float totalScore;
    private String reason;
    private String examPaperCode;
    private String studentCode;
    private String studentEmail;

    private LocalDateTime gradedAt;

    private Boolean flag; // 0 = không bị đánh cờ, 1 = kiểm tra đặc biệt
    private String flagReason;
}
