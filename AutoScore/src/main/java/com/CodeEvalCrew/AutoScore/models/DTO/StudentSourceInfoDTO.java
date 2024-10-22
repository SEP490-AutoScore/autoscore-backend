package com.CodeEvalCrew.AutoScore.models.DTO;

public class StudentSourceInfoDTO {
    private Long studentId;
    private String studentSourceCodePath;

    public StudentSourceInfoDTO() {}

    public StudentSourceInfoDTO(Long studentId, String studentSourceCodePath) {
        this.studentId = studentId;
        this.studentSourceCodePath = studentSourceCodePath;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentSourceCodePath() {
        return studentSourceCodePath;
    }

    public void setStudentSourceCodePath(String studentSourceCodePath) {
        this.studentSourceCodePath = studentSourceCodePath;
    }
}
