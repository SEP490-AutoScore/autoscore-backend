package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreCategoryDTO {
    private long excellent; // 9-10
    private long good;      // 8-9
    private long fair;      // 5-8
    private long poor;      // 4-5
    private long bad;       // 0-4
}