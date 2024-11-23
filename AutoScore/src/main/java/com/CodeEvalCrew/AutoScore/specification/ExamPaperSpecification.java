package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;

public class ExamPaperSpecification {
    @SuppressWarnings("unused")
    public static Specification<Exam_Paper> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }

    @SuppressWarnings("unused")
    public static Specification<Exam_Paper> hasTrueStatus(){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), true);
        };
    }

    @SuppressWarnings("unused")
    public static Specification<Exam_Paper> isUsedFalse(){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("isUsed"), false);
        };
    }
}
