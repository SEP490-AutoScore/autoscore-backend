package com.CodeEvalCrew.AutoScore.services.semesterService;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.CreateSemesterRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;

public interface ISemesterService {

    List<SemesterView> getAllSemester();

    SemesterView createNewSemester(CreateSemesterRequest request);
    
}
