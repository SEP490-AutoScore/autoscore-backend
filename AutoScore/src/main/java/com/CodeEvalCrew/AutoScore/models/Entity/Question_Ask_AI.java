package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class Question_Ask_AI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionAskAiId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private Long stepNo;

    private String aiName;

    private String purpose;

    private String aiKey;
}
