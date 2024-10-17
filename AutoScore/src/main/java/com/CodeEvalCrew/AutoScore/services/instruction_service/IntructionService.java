package com.CodeEvalCrew.AutoScore.services.instruction_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.InstructionsMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;
import com.CodeEvalCrew.AutoScore.models.Entity.Instructions;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.instruction_repository.IInstructionRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.specification.InstructionsSpecsification;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class IntructionService implements IIntructionService {

    @Autowired
    private final IInstructionRepository instructionRepository;

    @Autowired
    private final ISubjectRepository subjectRepository;

    public IntructionService(IInstructionRepository instructionRepository,
            ISubjectRepository subjectRepository) {
        this.instructionRepository = instructionRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public InstructionView getById(Long id) throws NotFoundException {
        InstructionView result;
        try {
            //check
            Instructions entity = checkEntityExistence(instructionRepository.findById(id), "Instructions", id);

            if (entity == null) {
                throw new NotFoundException();
            }

            return result = InstructionsMapper.INSTANCE.instructionToView(entity);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public List<InstructionView> getList(InstructionViewRequest request) throws NotFoundException, NoSuchElementException {
        List<InstructionView> result = new ArrayList<>();
        try {
            Specification<Instructions> spec = InstructionsSpecsification.hasForeignKey(request.getSubjectId(), "subject", "subjectId");

            //check
            Subject entity = checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject", request.getSubjectId());

            List<Instructions> listEntity = instructionRepository.findAll(spec);

            if (listEntity.isEmpty()) {
                throw new NoSuchElementException("No instruction found");
            }

            for (Instructions instructions : listEntity) {
                result.add(InstructionsMapper.INSTANCE.instructionToView(instructions));
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
    public InstructionView createNewInstruoction(InstructionCreateRequest request) throws NotFoundException {
        InstructionView result;
        try {
            //check
            Subject subject = checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject", request.getSubjectId());

            Instructions instruction = InstructionsMapper.INSTANCE.requestToEntity(request);

            //Update new instruction
            instruction.setSubject(subject);
            instruction.setCreatedAt(Util.getCurrentDateTime());
            instruction.setCreatedBy(Util.getAuthenticatedAccountId());

            instructionRepository.save(instruction);

            return result = InstructionsMapper.INSTANCE.instructionToView(instruction);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public InstructionView updateInstruction(Long id, InstructionCreateRequest request) throws NotFoundException {
        InstructionView result;
        try {
            //check instruction
            Instructions instruction = checkEntityExistence(instructionRepository.findById(id), "Instructions", id);

            //check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject", request.getSubjectId());

            //Update new instruction
            instruction.setSubject(subject);
            instruction.setCreatedAt(Util.getCurrentDateTime());
            instruction.setCreatedBy(Util.getAuthenticatedAccountId());

            instructionRepository.save(instruction);

            return result = InstructionsMapper.INSTANCE.instructionToView(instruction);
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
