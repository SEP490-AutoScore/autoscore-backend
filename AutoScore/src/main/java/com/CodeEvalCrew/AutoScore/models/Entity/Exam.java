package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;
    private String examCode;
    private LocalDateTime examAt;
    private LocalDateTime gradingAt;
    private LocalDateTime publishAt;
    private boolean status;
    @Enumerated(EnumType.STRING)
    private Exam_Type_Enum type;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    
    //Relationship
    //n-1 subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", nullable = true)
    private Subject subject;

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Exam_Paper> exam_papers;    

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Student> students;    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semesterId", nullable = true)
    private Semester semester;
}
