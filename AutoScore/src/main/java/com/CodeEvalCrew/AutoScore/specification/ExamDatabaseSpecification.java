package com.CodeEvalCrew.AutoScore.specification;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;

public class ExamDatabaseSpecification {
public static Specification<Exam_Database> hasForeignKey(long id,String joinTable, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.join(joinTable).get(joinAttribute), id);
        };
    }
}
