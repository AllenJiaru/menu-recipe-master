package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeFavorite;

public interface RecipeFavoriteService {
    void toggleFavorite(Long userId, Long recipeId, String recipeName, String recipeCover);
    boolean isFavorited(Long userId, Long recipeId);
    PageResponse<RecipeFavorite> getUserFavorites(Long userId, int page, int size);
    void removeFavorite(Long userId, Long recipeId);
}
