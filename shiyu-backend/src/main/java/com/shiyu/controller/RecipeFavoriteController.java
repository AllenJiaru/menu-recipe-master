package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeFavorite;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.RecipeFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class RecipeFavoriteController {

    @Autowired
    private RecipeFavoriteService recipeFavoriteService;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @PostMapping("/toggle")
    public ApiResponse<Void> toggleFavorite(@RequestBody Map<String, Object> body) {
        User user = getCurrentUser();
        Long recipeId = Long.valueOf(body.get("recipeId").toString());
        String recipeName = (String) body.get("recipeName");
        String recipeCover = (String) body.get("recipeCover");
        recipeFavoriteService.toggleFavorite(user.getId(), recipeId, recipeName, recipeCover);
        return ApiResponse.success();
    }

    @GetMapping("/check/{recipeId}")
    public ApiResponse<Map<String, Boolean>> checkFavorite(@PathVariable Long recipeId) {
        User user = getCurrentUser();
        boolean favorited = recipeFavoriteService.isFavorited(user.getId(), recipeId);
        return ApiResponse.success(Map.of("favorited", favorited));
    }

    @GetMapping
    public ApiResponse<PageResponse<RecipeFavorite>> getUserFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getCurrentUser();
        return ApiResponse.success(recipeFavoriteService.getUserFavorites(user.getId(), page, size));
    }
}
