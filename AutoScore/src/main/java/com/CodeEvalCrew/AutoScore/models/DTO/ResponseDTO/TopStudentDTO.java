package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TopStudentDTO {
    private String studentCode;
    private String studentEmail;
    private Float totalScore;
    private String examCode;
}
