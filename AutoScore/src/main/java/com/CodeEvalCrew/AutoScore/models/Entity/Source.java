package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Source {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long source_id;

    private String origin_source_path;

    private Timestamp import_time;

    //Relationship
    //1-n source detail
    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    private Set<Source_Detail> source_details;
}
