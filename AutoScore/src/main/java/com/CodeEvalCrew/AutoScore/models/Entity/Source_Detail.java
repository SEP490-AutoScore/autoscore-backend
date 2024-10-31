package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
public class Source_Detail {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sourceDetailId;

    private String studentSourceCodePath;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileCollectionPostman;

    private Boolean is_pass_first_progress;
    private Boolean is_pass_second_progress;
    private Boolean is_pass_third_progress;

    //Relationship
    //n-1 student
    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    //n-1 source
    @ManyToOne
    @JoinColumn(name = "sourceId", nullable = false)
    private Source source;
}
