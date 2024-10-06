package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteSubjectRequest {
    private Long subjectId; // ID của subject cần xóa
    private Long deletedBy; // ID của người thực hiện xóa
}
