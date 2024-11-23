package com.CodeEvalCrew.AutoScore.models.Entity;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class AI_Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiPromptId;

    private String content;

    private String languageCode;

    private String for_ai;

    private String type;

    private boolean status;

    @Nullable
    private Long parent;

}
