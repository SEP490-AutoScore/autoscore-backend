package com.CodeEvalCrew.AutoScore.services.source_grading_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.SourceGradingMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceView;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;

@Service
public class SourceGradingService implements  ISourceGradingService{
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private ISubjectRepository subjectREpository;

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public List<SourceView> getAllSource() {
        List<SourceView> result = new ArrayList<>();
        try {
            List<Source> sources = sourceRepository.findAll();

            if(sources.isEmpty()) throw new NoSuchElementException("No source found");

            for (Source source : sources) {
                SourceView view = SourceGradingMapper.INSTANCE.sourceToView(source);
                // Subject subject = checkEntityExistence(subjectREpository.findById(source.getExamPaper().g), "Subject", );

                result.add(view);
            }
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

}