package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewmanResult {
    private List<String> functionNames = new ArrayList<>();
    private List<Integer> totalPmTests = new ArrayList<>(); // Initialize with default empty list
}
