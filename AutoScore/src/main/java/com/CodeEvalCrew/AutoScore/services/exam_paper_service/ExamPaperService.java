package com.CodeEvalCrew.AutoScore.services.exam_paper_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamPaperMapper;
import com.CodeEvalCrew.AutoScore.mappers.ExamQuestionMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamPaperSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExamPaperService implements IExamPaperService {
    @Autowired
    private final IExamPaperRepository examPaperRepository;
    @Autowired
    private final IExamRepository examRepository;

    public ExamPaperService(IExamPaperRepository examPaperRepository,
                            IExamRepository examRepository) {
        this.examPaperRepository = examPaperRepository;
        this.examRepository = examRepository;
    }

    @Override
    public ExamPaperView getById(Long id) throws NotFoundException {
        ExamPaperView result;
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            return result = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public List<ExamPaperView> getList(ExamPaperViewRequest request) throws NotFoundException, NoSuchElementException{
        List<ExamPaperView> result = new ArrayList<>();
        try {
            //check exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            //crete spec
            Specification<Exam_Paper> spec = ExamPaperSpecification.hasForeignKey(request.getExamId(), "exam", "examId");
            spec.and(ExamPaperSpecification.hasTrueStatus());

            List<Exam_Paper> listEntities = examPaperRepository.findAll(spec); 
            
            if(listEntities.isEmpty()) throw new NoSuchElementException("No exam paper found");

            for (Exam_Paper exam_Paper : listEntities) {
                result.add(ExamPaperMapper.INSTANCE.examPAperToView(exam_Paper));
            }

            return result;
        } catch (NotFoundException | NoSuchElementException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }

    }

    @Override
    public ExamPaperView createNewExamPaper(ExamPaperCreateRequest request) throws NotFoundException {
        ExamPaperView result;
        try {
            //check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            //mapping
            Exam_Paper examPaper = ExamPaperMapper.INSTANCE.requestToExamPaper(request);

            //update side in4
            examPaper.setExam(exam);
            examPaper.setCreatedAt(Util.getCurrentDateTime());
            examPaper.setCreatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            return result = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamPaperView updateExamPaper(Long id, ExamPaperCreateRequest request) throws NotFoundException{
        ExamPaperView result;
        try {
            //check ExamPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            //check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            //check exam instuc

            //check exam db

            //update side in4
            examPaper.setExamPaperCode(request.getExamPaperCode());
            //examPaper.setExamInstruc
            //examPaper.serExamDB
            examPaper.setExam(exam);
            examPaper.setStatus(true);
            examPaper.setUpdatedAt(Util.getCurrentDateTime());
            examPaper.setUpdatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            return result = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamPaperView deleteExamPaper(Long id) throws NotFoundException {
        ExamPaperView result;
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            examPaper.setStatus(false);
            examPaper.setDeletedAt(Util.getCurrentDateTime());
            examPaper.setDeletedBy(Util.getAuthenticatedAccountId());

            return result = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }
    
}
