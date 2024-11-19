package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Important;

public class ImportantSpecification {
    @SuppressWarnings("unused")
    public static Specification<Important> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }

    @SuppressWarnings("unused")
    public static Specification<Important> hasTrueStatus(){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), true);
        };
    }
}
