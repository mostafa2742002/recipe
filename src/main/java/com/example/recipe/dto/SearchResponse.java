package com.example.recipe.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<RecipeSearchResult> recipes;
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;
}
