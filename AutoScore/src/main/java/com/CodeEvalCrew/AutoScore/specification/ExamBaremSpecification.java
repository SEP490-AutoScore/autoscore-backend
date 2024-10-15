package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;

public class ExamBaremSpecification {
    public static Specification<Exam_Barem> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }
}
