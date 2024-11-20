package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class AI_Info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiInfoId;

    @NotNull
    private String aiApiKey;

    @NotNull
    private String aiName;

    private Long createdBy;
    
    private String purpose;

    // Relationship
    @OneToMany(mappedBy = "aiInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Content> contents;
}
