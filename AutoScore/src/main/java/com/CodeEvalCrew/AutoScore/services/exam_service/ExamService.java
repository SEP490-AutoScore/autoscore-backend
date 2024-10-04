package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamSpecification;

@Service
public class ExamService implements IExamService {

    private final IExamRepository examRepository;

    public ExamService(IExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public Exam getExamById(long id) throws Exception, NotFoundException {
        Exam result = new Exam();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                // result.setMessage("Not found exam with id: "+ id);
                throw new NotFoundException();
            }
            result = exam;
        } catch (NotFoundException e) {
            throw new NotFoundException();
            // return result;
        }

        return result;
    }

    @Override
    public List<ExamViewResponseDTO> GetExam(ExamViewRequestDTO request) throws Exception {
        List<ExamViewResponseDTO> result = new ArrayList<>();
        try {
            Specification<Exam> spec = createSpecificationForGet(request);
            List<Exam> listExams = examRepository.findAll(spec);

            for (Exam exam : listExams) {
                result.add(new ExamViewResponseDTO(exam));
            }

            if (result.isEmpty()) {
                throw new NoSuchElementException("No records found for the given request.");
            }
        } catch (NoSuchElementException e) {
            throw e; // Re-throw custom exception for no records
        } catch (Exception e) {
            throw new Exception("An error occurred while fetching exam records.", e);
        }

        return result;
    }

    @Override
    public ExamViewResponseDTO CreateNewExam(ExamCreateRequestDTO entity) throws Exception{
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            //checkc campus

            //check subject

            //check exist exam
            Specification<Exam> spec = createSpecificationForExistedExamByCode(entity.getExamCode());
            boolean isExisted = examRepository.exists(spec);
            if(isExisted){
                throw new Exception("Exam code existed");
            }

            //mapping exam
            Exam exam = new Exam();

            //create new exam
            examRepository.save(exam);
        } catch (Exception e) {
            throw new Exception("Error");
        }

        return result;
    }

// <editor-fold desc="get exam func helper">
    private Specification<Exam> createSpecificationForGet(ExamViewRequestDTO request) {
        Specification<Exam> spec = Specification.where(null);
        
        if (!request.getSearchString().isBlank()) {
            spec = spec.or(ExamSpecification.hasExamCode(request.getSearchString()))
                       .or(ExamSpecification.hasSemester(request.getSearchString()));
        }

        if (request.getCampusId() != 0) {
            spec.and(ExamSpecification.hasCampusId(request.getCampusId()));
        }

        if (request.getSubjectId() != 0) {
            spec.and(ExamSpecification.hasSubjectId(request.getSubjectId()));
        }

        return spec;
    }
// </editor-fold>

// <editor-fold desc="create exam func helper">
    private Specification<Exam> createSpecificationForExistedExamByCode(String examCode){
        Specification<Exam> spec = Specification.where(null);
        
        spec.and(ExamSpecification.hasExamCode(examCode));

        return spec;
    }


// </editor-fold>


}
