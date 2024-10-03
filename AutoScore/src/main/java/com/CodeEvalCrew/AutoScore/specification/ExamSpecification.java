package com.CodeEvalCrew.AutoScore.specification;

import java.sql.Timestamp;

import org.springframework.data.jpa.domain.Specification;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

public class ExamSpecification {
    public static Specification<Exam> hasExamCode(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("examCode"), searchString);
        };
    }

    public static Specification<Exam> hasSemester(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("semesterName"), searchString);
        };
    }

    public static Specification<Exam> hasExamAt(Timestamp timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("examAt"), timestamp);
        };
    }
    public static Specification<Exam> hasGradingAt(Timestamp timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("gradingAt"), timestamp);
        };
    }
    public static Specification<Exam> hasPublishAt(Timestamp timestamp) {
        return (root, query, criteriaBuilder) -> {
            if (timestamp == null) {
                return criteriaBuilder.conjunction(); // No filtering if null or empty
            }
            return criteriaBuilder.equal(root.get("publishAt"), timestamp);
        };
    }
    
    // Filter by Campus id (foreign key condition)
    public static Specification<Exam> hasCampusId(long id) {
        return (root, query, criteriaBuilder) -> {
            // Join with the campus entity
            return criteriaBuilder.equal(root.join("campus").get("campusId"), id);
        };
    }

    public static Specification<Exam> hasSubjectId(long id) {
        return (root, query, criteriaBuilder) -> {
            // Join with the subject entity
            return criteriaBuilder.equal(root.join("subject").get("subjectId"), id);
        };
    }

}
