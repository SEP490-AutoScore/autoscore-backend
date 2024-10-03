package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginateEntity {
    private int currentPage = 1; // Default to page 1
    private int pageItem = 10;   // Default to 10 items per page
}
