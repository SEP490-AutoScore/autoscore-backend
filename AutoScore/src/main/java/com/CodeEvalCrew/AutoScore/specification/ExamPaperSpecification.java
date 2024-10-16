package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;

public class ExamPaperSpecification {
    public static Specification<Exam_Barem> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }

    public static Specification<Exam_Barem> hasTrueStatus(){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), true);
        };
    }
}
