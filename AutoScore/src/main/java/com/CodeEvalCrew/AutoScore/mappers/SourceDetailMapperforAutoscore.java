package com.CodeEvalCrew.AutoScore.mappers;

import org.springframework.stereotype.Component;

import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;

@Component
public class SourceDetailMapperforAutoscore {
    public synchronized StudentSourceInfoDTO toDTO(Source_Detail sourceDetail) {
        if (sourceDetail == null || sourceDetail.getStudent() == null) {
            return new StudentSourceInfoDTO(null, null, null);
        }
        return new StudentSourceInfoDTO(
            sourceDetail.getSourceDetailId(), // Map sourceDetailId
            sourceDetail.getStudent().getStudentId(),
            sourceDetail.getStudentSourceCodePath()
        );
    }
}
