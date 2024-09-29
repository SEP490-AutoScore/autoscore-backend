package com.CodeEvalCrew.AutoScore.models.Entity;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AI_Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ai_prompt_id;

    private String content;

    private String language_code;

    private String for_ai;

    private String type;

    private boolean status;

    @Nullable
    private long parent;

}
