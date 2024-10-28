package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SonarQubeRunnerRequest {
    private String projectKey;
    private String source;
    private String hostURL;
    private String token;
}
