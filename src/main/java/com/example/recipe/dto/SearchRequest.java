package com.example.recipe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class SearchRequest {

    private String searchText;

    private String ingredient;

    private String cuisine;

    private String dietaryPreference;

    private Long maxPrepTime;

    private String sortBy = "relevance";

    @Min(value = 0, message = "Page must be 0 or greater")
    private Integer page = 0;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    private Integer limit = 10;
}
