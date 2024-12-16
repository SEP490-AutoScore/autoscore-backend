package com.CodeEvalCrew.AutoScore.services.semesterService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SemesterMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.CreateSemesterRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.Entity.Semester;
import com.CodeEvalCrew.AutoScore.repositories.semester_repository.SemesterRepository;

@Service
public class SemesterService implements ISemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    public List<SemesterView> getAllSemester() {
        List<SemesterView> result = new ArrayList<>();
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
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public SemesterView createNewSemester(CreateSemesterRequest request) {
        SemesterView result;
        try {

            boolean existCode = semesterRepository.existsBySemesterCode(request.getSemesterCode());

            if (existCode) {
                throw new IllegalArgumentException("Semester Code already exist");
            }

            Semester newSemester = new Semester();
            newSemester.setSemesterCode(request.getSemesterCode());
            newSemester.setSemesterName(request.getSemesterName());
            newSemester.setStatus(true);

            semesterRepository.save(newSemester);

            result = SemesterMapper.INSTANCE.semesterToView(newSemester);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

//check entiy func helper
    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws Exception {
        return entity.orElseThrow(() -> new NoSuchElementException(entityName + " id: " + entityId + " not found"));
    }

    @Override
    public SemesterView updateSemesterInfo(Long semesterId, CreateSemesterRequest request) throws Exception {
        SemesterView result;
        try {
            Semester semester = checkEntityExistence(semesterRepository.findById(semesterId), "Semester", semesterId);
            semester.setSemesterName(request.getSemesterName());
            semesterRepository.save(semester);

            result = SemesterMapper.INSTANCE.semesterToView(semester);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public SemesterView getSemesterById(Long semesterId) throws Exception {
        try {
            Semester semester = checkEntityExistence(semesterRepository.findById(semesterId), "Semester", semesterId);
            return SemesterMapper.INSTANCE.semesterToView(semester);
        } catch (Exception e) {
            throw e;
        }
    }

}
