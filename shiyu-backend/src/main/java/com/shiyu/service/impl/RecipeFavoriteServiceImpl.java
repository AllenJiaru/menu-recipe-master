package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeFavorite;
import com.shiyu.mapper.RecipeFavoriteMapper;
import com.shiyu.service.RecipeFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecipeFavoriteServiceImpl implements RecipeFavoriteService {

    @Autowired
    private RecipeFavoriteMapper recipeFavoriteMapper;

    @Override
    public void toggleFavorite(Long userId, Long recipeId, String recipeName, String recipeCover) {
        RecipeFavorite existing = recipeFavoriteMapper.findUserFavorite(userId, recipeId);
        if (existing != null) {
            recipeFavoriteMapper.deleteById(existing.getId());
        } else {
            RecipeFavorite favorite = new RecipeFavorite();
            favorite.setUserId(userId);
            favorite.setRecipeId(recipeId);
            favorite.setRecipeName(recipeName);
            favorite.setRecipeCover(recipeCover);
            favorite.setCreateTime(LocalDateTime.now());
            recipeFavoriteMapper.insert(favorite);
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long recipeId) {
        RecipeFavorite existing = recipeFavoriteMapper.findUserFavorite(userId, recipeId);
        return existing != null;
    }

    @Override
    public PageResponse<RecipeFavorite> getUserFavorites(Long userId, int page, int size) {
        List<RecipeFavorite> allFavorites = recipeFavoriteMapper.findUserFavoritesWithRecipe(userId);
        int total = allFavorites.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<RecipeFavorite> pageRecords = from < total ? allFavorites.subList(from, to) : List.of();
        return PageResponse.of(pageRecords, (long) total, page, size);
    }

    @Override
    public void removeFavorite(Long userId, Long recipeId) {
        RecipeFavorite existing = recipeFavoriteMapper.findUserFavorite(userId, recipeId);
        if (existing != null) {
            recipeFavoriteMapper.deleteById(existing.getId());
        }
    }
}
