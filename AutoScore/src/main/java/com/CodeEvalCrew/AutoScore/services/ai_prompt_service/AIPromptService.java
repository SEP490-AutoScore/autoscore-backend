package com.CodeEvalCrew.AutoScore.services.ai_prompt_service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.AIPromptMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AIPrompt.CreatePromptDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptView;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;
import com.CodeEvalCrew.AutoScore.repositories.AI_prompt_reposotiry.IAIPromptRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.specification.AIPromptSpecifacation;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AIPromptService implements IAIPromptService {
    @Autowired
    private final IAIPromptRepository promptRepository;
    // @Autowired
    // private final IAccountRepository accountRepository;
    // @Autowired
    // private final Util util;

    public AIPromptService(IAIPromptRepository promptRepository
                                // ,IAccountRepository accountRepository
                                // ,Util util
                                ) {
        this.promptRepository = promptRepository;
        // this.accountRepository = accountRepository;
        // this.util = new Util(accountRepository);
    }

    @Override
    public AIPromptView getPromptById(long id) throws Exception {
        AIPromptView result;
        try {
            AI_Prompt entity = (AI_Prompt) promptRepository.findById(id).get();

            if(entity == null){
                throw new NoSuchElementException("AI prompt not found");
            }// no element found

            //mapper
            result = AIPromptMapper.INSTANCE.entityToPromptView(entity);
            //return
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        
    }

    @Override
    public List<AIPromptView> getPromptByUser(long userId) throws Exception {
        List<AIPromptView> result;
        try {
            //create spec
            Specification<AI_Prompt> spec = createSpecificationForGetByUser(userId);

            List<AI_Prompt> listEntity = promptRepository.findAll(spec);

            if(listEntity.isEmpty()){
                throw new NoSuchElementException("AI prompt not found");
            }// no element found
            
            //mapper
            result = mapperListEntityToView(listEntity);

            //return
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public AIPromptView createNewPrompt(CreatePromptDTO request) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private Specification<AI_Prompt> createSpecificationForGetByUser(long id) {
        Specification<AI_Prompt> spec = Specification.where(null);

        if (id != 0) {  
            spec.or(AIPromptSpecifacation.hasIdOrParent(id, "aiPromptId"));
            spec.or(AIPromptSpecifacation.hasIdOrParent(id, "parent"));
        }
        return spec;
    }

    private List<AIPromptView> mapperListEntityToView(List<AI_Prompt> listEntity) {
        return listEntity.stream()
                         .map(AIPromptMapper.INSTANCE::entityToPromptView)
                         .collect(Collectors.toList());
    }
    

}
