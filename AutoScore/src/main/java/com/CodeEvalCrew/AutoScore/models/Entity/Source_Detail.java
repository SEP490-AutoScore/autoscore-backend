package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.ArrayList;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Source_Detail {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sourceDetailId;

    private String studentSourceCodePath;
    @Lob
    private String normalizedCode;
    @OneToMany(mappedBy = "sourceDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NGram> nGrams = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileCollectionPostman;

    @Enumerated(EnumType.STRING)
    private Exam_Type_Enum type;

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
