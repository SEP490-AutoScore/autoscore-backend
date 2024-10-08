package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.campus_repository.ICampusRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamSpecification;

import jakarta.transaction.Transactional;

@Service
public class ExamService implements IExamService {
    @Autowired
    private final IExamRepository examRepository;

    @Autowired
    private final ICampusRepository campusRepository;

    @Autowired
    private final ISubjectRepository subjectRepository;

    @Autowired
    private final IAccountRepository accountRepository;

    public ExamService(IExamRepository examRepository,
            ICampusRepository campusRepository,
            ISubjectRepository subjectRepository,
            IAccountRepository accountRepository) {
        this.examRepository = examRepository;
        this.campusRepository = campusRepository;
        this.subjectRepository = subjectRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ExamViewResponseDTO getExamById(long id) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                throw new NotFoundException("Exam id:" + id + " not found");
            }
            result = new ExamViewResponseDTO(exam);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
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
                result.add(ExamMapper.INSTANCE.examToViewResponse(exam));
            }

            if (result.isEmpty()) {
                throw new NoSuchElementException("No records");
            }
        } catch (NoSuchElementException e) {
            throw e; // Re-throw custom exception for no records
        } catch (Exception e) {
            throw new Exception("An error occurred while fetching exam records.", e);
        }

        return result;
    }

    @Override
    @Transactional
    public ExamViewResponseDTO createNewExam(ExamCreateRequestDTO entity) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            // Check campus
            Campus campus = checkEntityExistence(campusRepository.findById(entity.getCampusId()), "Campus", entity.getCampusId());

            // Check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(entity.getSubjectId()), "Subject", entity.getSubjectId());

            // Check account
            Account account = checkEntityExistence(accountRepository.findById(entity.getAccountId()), "Account", entity.getAccountId());

            // //check exist exam
            // Optional<Exam> optionExam = examRepository.findById(entity.getExamId());
            // if (optionExam.isPresent()) {
            //     throw new NotFoundException("Exam id: " + entity.getExamId() + " not found");
            // }

            //mapping exam
            Exam exam = ExamMapper.INSTANCE.requestToExam(entity);
            exam.setCampus(campus);
            exam.setSubject(subject);
            exam.setCreatedAt(LocalDateTime.now());
            exam.setStatus(true);
            exam.setCreatedBy(account.getAccountId());

            //create new exam
            examRepository.save(exam);

        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public ExamViewResponseDTO updateExam(ExamCreateRequestDTO entity) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            // Check campus
            Campus campus = checkEntityExistence(campusRepository.findById(entity.getCampusId()), "Campus", entity.getCampusId());

            // Check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(entity.getSubjectId()), "Subject", entity.getSubjectId());

            //check exist exam
            Exam exam = checkEntityExistence(examRepository.findById(entity.getExamId()), "Exam", entity.getExamId());

            //update exam 
            exam.setExamCode(entity.getExamCode());
            exam.setExamAt(entity.getExamAt());
            exam.setGradingAt(entity.getGradingAt());
            exam.setPublishAt(entity.getPublishAt());
            exam.setCampus(campus);
            exam.setSubject(subject);

            //create new exam
            examRepository.save(exam);

            result = ExamMapper.INSTANCE.examToViewResponse(exam);

            //mapping exam
            // Exam exam = new Exam();
        }catch(NotFoundException nfe){
            throw nfe;
        } 
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return result;
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
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
    private Specification<Exam> createSpecificationForExistedExamByCode(String examCode) {
        Specification<Exam> spec = Specification.where(null);
        spec.and(ExamSpecification.hasExamCode(examCode));
        return spec;
    }

// </editor-fold>
}
