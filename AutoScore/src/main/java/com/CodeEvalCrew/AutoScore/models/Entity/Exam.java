package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    
    //Relationship
    //n-1 subject
    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Exam_Paper> exam_papers;    

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Student> students;    

    @ManyToOne
    @JoinColumn(name = "semesterId", nullable = false)
    private Semester semester;
}
