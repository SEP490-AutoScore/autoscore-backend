package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;

public class GradingSpecification {
@SuppressWarnings("unused")
    public static Specification<GradingProcess> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }
}
