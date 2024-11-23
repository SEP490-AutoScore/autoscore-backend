package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;

public class AIPromptSpecifacation {

    public static Specification<AI_Prompt> hasIdOrParent(long id,String attributeName){
        return (root, query, criteriaBuilder) -> {
            if (id == 0) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get(attributeName), id);
        };
    }

}
