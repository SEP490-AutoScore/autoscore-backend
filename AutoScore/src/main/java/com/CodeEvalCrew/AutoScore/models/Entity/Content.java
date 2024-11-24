package com.CodeEvalCrew.AutoScore.models.Entity;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "content")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @NotNull
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String questionAskAiContent;

    private Long orderPriority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose_Enum purpose;

} 