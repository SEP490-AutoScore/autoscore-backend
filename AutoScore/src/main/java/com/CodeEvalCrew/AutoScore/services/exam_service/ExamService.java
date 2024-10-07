package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

@Service
public class ExamService implements IExamService {

    private final IExamRepository examRepository;

    @Autowired
    private final ICampusRepository campusRepository;

    @Autowired
    private final ISubjectRepository subjectRepository;

    @Autowired
    private final IAccountRepository accountRepository;

    public ExamService(IExamRepository examRepository
                        ,ICampusRepository campusRepository
                        ,ISubjectRepository subjectRepository
                        ,IAccountRepository accountRepository) {
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
                // result.setMessage("Not found exam with id: "+ id);
                throw new NotFoundException();
            }
            result = new ExamViewResponseDTO(exam);
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
                result.add(ExamMapper.INSTANCE.examToViewResponse(exam));
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
            //check campus
            Optional<Campus> optionCampus = campusRepository.findById(entity.getCampusId());
            if(!optionCampus.isPresent()){
                throw new Exception("Campus has not existed");
            }
            
            //check subject
            Optional<Subject> optionSubject = subjectRepository.findById(entity.getSubjectId());
            if(!optionSubject.isPresent()){
                throw new Exception("Subject has not existed");
            }

            //check aacount
            Optional<Account> optionalAccount = accountRepository.findById(entity.getAccountId());
            if(!optionalAccount.isPresent()){
                throw new Exception("Account has not existed");
            }

            //check exist exam
            // Specification<Exam> spec = createSpecificationForExistedExamByCode(entity.getExamCode());
            // boolean isExisted = examRepository.exists(spec);
            // if(isExisted){
            //     throw new Exception("Exam code existed");
            // }

            Optional<Exam> optionExam = examRepository.findById(entity.getExamId());
            if(optionExam.isPresent()){
                throw new Exception("Exam existed");
            }

            //mapping exam
            Exam exam = ExamMapper.INSTANCE.requestToExam(entity);
            exam.setCampus(optionCampus.get());
            exam.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            exam.setSubject(optionSubject.get());
            exam.setAccount(optionalAccount.get());
            exam.setStatus(true);
            exam.setCreatedBy(entity.getAccountId());

            //create new exam
            examRepository.save(exam);

        } catch (Exception e) {
            throw new Exception("Error");
        }

        return result;
    }

    @Override
    public ExamViewResponseDTO updateExam(ExamCreateRequestDTO entity) throws Exception {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            //check campus
            Optional<Campus> optionCampus = campusRepository.findById(entity.getCampusId());
            if(!optionCampus.isPresent()){
                throw new Exception("Campus has not existed");
            }
            
            //check subject
            Optional<Subject> optionSubject = subjectRepository.findById(entity.getSubjectId());
            if(!optionSubject.isPresent()){
                throw new Exception("Subject has not existed");
            }

            //check exist exam
            Optional<Exam> optionExam = examRepository.findById(entity.getExamId());
            if(!optionExam.isPresent()){
                throw new Exception("Subject has not existed");
            }
            
            //update exam 
            Exam exam = optionExam.get();
            exam.setExamCode(entity.getExamCode());
            exam.setExamAt(entity.getExamAt());
            exam.setGradingAt(entity.getGradingAt());
            exam.setPublishAt(entity.getPublishAt());
            exam.setCampus(optionCampus.get());
            exam.setSubject(optionSubject.get());

            //create new exam
            examRepository.save(exam);

            result = ExamMapper.INSTANCE.examToViewResponse(exam);

            //mapping exam
            // Exam exam = new Exam();

        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
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
