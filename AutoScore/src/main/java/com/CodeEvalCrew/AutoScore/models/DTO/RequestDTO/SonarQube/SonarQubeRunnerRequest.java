package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SonarQubeRunnerRequest {
    private String projectKey;
    private String source;
    private String hostURL;
    private String token;
}
