package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubjectRequest {

    private Long subjectId; // ID của subject cần cập nhật
    private String subjectName; // Tên subject
    private String subjectCode; // Mã subject
    private Long updateBy; // ID của người cập nhật
    private Long departmentId; // ID của department

}
