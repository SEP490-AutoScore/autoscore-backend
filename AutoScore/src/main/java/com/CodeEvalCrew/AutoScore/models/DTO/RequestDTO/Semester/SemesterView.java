package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SemesterView {
    private Long semesterId;
    private String semesterCode;
    private String semesterName;
}
