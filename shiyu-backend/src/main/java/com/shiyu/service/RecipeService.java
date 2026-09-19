package com.shiyu.service;

import com.shiyu.dto.request.RecipeCreateRequest;
import com.shiyu.dto.request.RecipeUpdateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeCategory;
import java.util.List;

public interface RecipeService {
    PageResponse<Recipe> getRecipes(Integer page, Integer size, Long categoryId, String keyword, Integer difficulty, Long coupleId, Integer status);
    Recipe getRecipeById(Long id);
    Recipe createRecipe(RecipeCreateRequest request);
    Recipe updateRecipe(Long id, RecipeUpdateRequest request);
    void deleteRecipe(Long id);
    void batchDelete(java.util.List<Long> ids);
    void batchUpdateStatus(java.util.List<Long> ids, Integer status);
    void updateFavorite(Long id);
    void updateStatus(Long id, Integer status);
    List<RecipeCategory> getCategories();
    List<Recipe> getRandomRecipes(Integer count, Long coupleId);
}
