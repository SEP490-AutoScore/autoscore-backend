package com.CodeEvalCrew.AutoScore.services.exam_question_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamQuestionMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamQuestionView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamQuestionSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExamQuestionService implements IExamQuestionService {

    @Autowired
    private final IExamQuestionRepository examQuestionRepository;
    @Autowired
    private final IExamPaperRepository examPaperRepository;

    public ExamQuestionService(IExamQuestionRepository examQuestionRepository,
            IExamPaperRepository examPaperRepository) {
        this.examQuestionRepository = examQuestionRepository;
        this.examPaperRepository = examPaperRepository;
    }

    @Override
    public ExamQuestionView getById(Long id) throws NotFoundException {
        ExamQuestionView result;
        try {

            Exam_Question examQuestion = checkEntityExistence(examQuestionRepository.findById(id), "Exam Question", id);

            return result = ExamQuestionMapper.INSTANCE.examQuestionToView(examQuestion);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public List<ExamQuestionView> getList(ExamQuestionViewRequest request) throws NotFoundException {
        List<ExamQuestionView> result = new ArrayList<>();
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(request.getExamPaperId()), "Exam Paper", request.getExamPaperId());

            Specification<Exam_Question> spec = ExamQuestionSpecification.hasForeignKey(request.getExamPaperId(), "exam_paper", "examPaperId");
            spec.and(ExamQuestionSpecification.hasTrueStatus());

            List<Exam_Question> listEntity = examQuestionRepository.findAll(spec);

            if (listEntity.isEmpty()) {
                throw new NoSuchElementException("no question found");
            }

            for (Exam_Question exam_Question : listEntity) {
                result.add(ExamQuestionMapper.INSTANCE.examQuestionToView(exam_Question));
            }

            return result;
        } catch (NoSuchElementException | NotFoundException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamQuestionView createNewExamQuestion(ExamQuestionCreateRequest request) throws NotFoundException {
        ExamQuestionView result;
        try {
            // check examPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(request.getExamPaperId()), "Exam Paper", request.getExamPaperId());

            //createnew
            Exam_Question examQuestion = ExamQuestionMapper.INSTANCE.requestToExamQuestion(request);

            //update
            examQuestion.setExamPaper(examPaper);
            examQuestion.setCreatedAt(Util.getCurrentDateTime());
            examQuestion.setCreatedBy(Util.getAuthenticatedAccountId());

            examQuestionRepository.save(examQuestion);

            return result = ExamQuestionMapper.INSTANCE.examQuestionToView(examQuestion);
        } catch (NotFoundException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamQuestionView updateExamQuestion(Long id, ExamQuestionCreateRequest request) throws NotFoundException {
        ExamQuestionView result;
        try {
            //check examQuestion
            Exam_Question examQuestion = checkEntityExistence(examQuestionRepository.findById(id), "Exam question", id);

            // check examPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(request.getExamPaperId()), "Exam Paper", request.getExamPaperId());

            //update
            examQuestion.setQuestionContent(request.getQuestionContent());
            examQuestion.setQuestionNumber(request.getQuestionNumber());
            examQuestion.setMaxScore(request.getMaxScore());
            examQuestion.setType(request.getType());
            examQuestion.setExamPaper(examPaper);
            examQuestion.setUpdatedAt(Util.getCurrentDateTime());
            examQuestion.setUpdatedBy(Util.getAuthenticatedAccountId());

            examQuestionRepository.save(examQuestion);

            return result = ExamQuestionMapper.INSTANCE.examQuestionToView(examQuestion);
        } catch (NotFoundException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamQuestionView deleteExamQuestion(Long id) throws NotFoundException {
        ExamQuestionView result;
        try {
            //check examQuestion
            Exam_Question examQuestion = checkEntityExistence(examQuestionRepository.findById(id), "Exam question", id);

            //update
            examQuestion.setStatus(false);
            examQuestion.setDeletedAt(Util.getCurrentDateTime());
            examQuestion.setDeletedBy(Util.getAuthenticatedAccountId());

            examQuestionRepository.save(examQuestion);

            return result = ExamQuestionMapper.INSTANCE.examQuestionToView(examQuestion);
        } catch (NotFoundException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

}
