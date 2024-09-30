package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class Source {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sourceId;

    private String originSourcePath;

    private Timestamp importTime;

    //Relationship
    //1-n source detail
    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    private Set<Source_Detail> sourceDetails;
}
