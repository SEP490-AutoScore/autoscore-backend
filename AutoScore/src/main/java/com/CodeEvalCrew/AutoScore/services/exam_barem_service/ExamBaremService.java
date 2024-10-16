package com.CodeEvalCrew.AutoScore.services.exam_barem_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamBaremMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremCreate;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamBaremView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamBaremRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamBaremSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExamBaremService implements IExamBaremService {

    @Autowired
    private final IExamBaremRepository examBaremRepository;
    @Autowired
    private final IExamQuestionRepository examQuestionRepository;
    // @Autowired
    // private final Util util;

    public ExamBaremService(IExamBaremRepository examBaremRepository,
            IAccountRepository accountRepository,
            IExamQuestionRepository examQuestionRepository,
            IEmployeeRepository employeeRepository) {
        this.examBaremRepository = examBaremRepository;
        this.examQuestionRepository = examQuestionRepository;
        // this.util = new Util(employeeRepository);
    }

    @Override
    public ExamBaremView getExamById(Long id) throws NotFoundException {
        ExamBaremView result;
        try {

            Exam_Barem examBarem = examBaremRepository.findById(id).get();
            if (examBarem == null) {
                throw new NotFoundException("Exam barem id:" + id + " not found");
            }
            result = ExamBaremMapper.INSTANCE.examBaremToView(examBarem);
            return result;

        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }

    }

    @Override
    public List<ExamBaremView> getList(ExamBaremViewRequest request) {
        List<ExamBaremView> result = new ArrayList<>();
        try {

            Specification<Exam_Barem> spec = ExamBaremSpecification.hasForeignKey(request.getExamQuestionId(), "exam_question", "examQuestionId");
            List<Exam_Barem> listExamBarems = examBaremRepository.findAll(spec);
            if (result.isEmpty()) {
                throw new NoSuchElementException("No exam barem found");
            }
            for (Exam_Barem examBarem : listExamBarems) {
                result.add(ExamBaremMapper.INSTANCE.examBaremToView(examBarem));
            }
            return result;
        } catch (NoSuchElementException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamBaremView createNewExamBarem(ExamBaremCreate request) throws NotFoundException {
        ExamBaremView result;
        try {
            //check exam question
            Exam_Question examQuestion = checkEntityExistence(examQuestionRepository.findById(request.getExamQuestionId()), "Exam question", request.getExamQuestionId());

            //create new exam
            Exam_Barem examBarem = ExamBaremMapper.INSTANCE.examBaremCreateRequestToEntity(request);

            examBarem.setExamQuestion(examQuestion);

            //update creater craetedate
            examBarem.setCreatedAt(Util.getCurrentDateTime());
            examBarem.setCreatedBy(Util.getAuthenticatedAccountId());

            //save exam
            examBaremRepository.save(examBarem);

            //mapping 
            return result = ExamBaremMapper.INSTANCE.examBaremToView(examBarem);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamBaremView updateExamBarem(Long id, ExamBaremCreate request) throws NotFoundException {
        ExamBaremView result;
        try {
            // check exam barem
            Exam_Barem examBarem = checkEntityExistence(examBaremRepository.findById(id), "Exam Barem", id);

            //check exam question
            Exam_Question examQuestion = checkEntityExistence(examQuestionRepository.findById(request.getExamQuestionId()), "Exam question", request.getExamQuestionId());

            //update
            examBarem.setExamQuestion(examQuestion);
            examBarem.setBaremContent(request.getBaremContent());
            examBarem.setBaremMaxScore(request.getBaremMaxScore());
            examBarem.setBaremURL(request.getBaremURL());
            examBarem.setUpdatedAt(Util.getCurrentDateTime());
            examBarem.setUpdatedBy(Util.getAuthenticatedAccountId());

            //save exam
            examBaremRepository.save(examBarem);

            //mapping 
            return result = ExamBaremMapper.INSTANCE.examBaremToView(examBarem);
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
