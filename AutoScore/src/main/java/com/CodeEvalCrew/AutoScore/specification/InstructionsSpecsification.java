package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Instructions;

public class InstructionsSpecsification {
    public static Specification<Instructions> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }

    public static Specification<Instructions> hasTrueStatus(){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), true);
        };
    }
}