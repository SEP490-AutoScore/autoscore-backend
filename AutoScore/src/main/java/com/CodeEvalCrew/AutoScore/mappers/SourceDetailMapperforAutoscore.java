package com.CodeEvalCrew.AutoScore.mappers;
import org.springframework.stereotype.Component;

import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
@Component
public class SourceDetailMapperforAutoscore {
     public StudentSourceInfoDTO toDTO(Source_Detail sourceDetail) {
        return new StudentSourceInfoDTO(
            sourceDetail.getStudent().getStudentId(),
            sourceDetail.getStudentSourceCodePath()
        );
    }
    
}
