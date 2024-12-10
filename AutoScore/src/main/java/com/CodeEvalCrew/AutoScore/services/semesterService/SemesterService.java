package com.CodeEvalCrew.AutoScore.services.semesterService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SemesterMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.CreateSemesterRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.Entity.Semester;
import com.CodeEvalCrew.AutoScore.repositories.semester_repository.SemesterRepository;

@Service
public class SemesterService implements ISemesterService{
    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    public List<SemesterView> getAllSemester() {
        List<SemesterView> result =new ArrayList<>();
        try {
            List<Semester> semesters = semesterRepository.findAll();

            if (semesters.isEmpty()) {
                throw new NoSuchElementException("No semester found");
            }

            for (Semester semester : semesters) {
                result.add(new SemesterView(semester.getSemesterId(), semester.getSemesterCode(), semester.getSemesterName()));
            }
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        }catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public SemesterView createNewSemester(CreateSemesterRequest request) {
        SemesterView result;
        try {
            Semester semester = new Semester();
            semester.setSemesterCode(request.getSemesterCode());
            semester.setSemesterName(request.getSemesterName());
            semester.setStatus(true);

            semesterRepository.save(semester);

            result = SemesterMapper.INSTANCE.semesterToView(semester);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

}
