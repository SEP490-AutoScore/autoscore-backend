package com.CodeEvalCrew.AutoScore.specification;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

public class ExamSpecification {
    public static Specification<Exam> hasExamCode(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isBlank()) {
                return criteriaBuilder.conjunction(); // No filtering if null or blank
            }
            String pattern = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("examCode")), pattern);
        };
    }

    public static Specification<Exam> hasSemester(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isBlank()) {
                return criteriaBuilder.conjunction(); // No filtering if null or blank
            }
            String pattern = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("semesterName")), pattern);
        };
    }

    public static Specification<Exam> hasExamAt(LocalDateTime timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("examAt"), timestamp);
        };
    }
    public static Specification<Exam> hasGradingAt(LocalDateTime timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("gradingAt"), timestamp);
        };
    }
    public static Specification<Exam> hasPublishAt(LocalDateTime timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("publishAt"), timestamp);
        };
    }

    public static Specification<Exam> hasSubjectId(long id) {
        return (root, query, criteriaBuilder) -> {
            // Join with the subject entity
            return criteriaBuilder.equal(root.join("subject").get("subjectId"), id);
        };
    }

}
