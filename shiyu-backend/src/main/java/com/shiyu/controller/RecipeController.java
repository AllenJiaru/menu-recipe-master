package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.BatchRequest;
import com.shiyu.dto.request.RecipeCreateRequest;
import com.shiyu.dto.request.RecipeUpdateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeCategory;
import com.shiyu.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @OperationLog(action = "QUERY", target = "菜谱")
    @GetMapping
    public ApiResponse<PageResponse<Recipe>> getRecipes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Long coupleId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(recipeService.getRecipes(page, size, categoryId, keyword, difficulty, coupleId, status));
    }

    @OperationLog(action = "QUERY", target = "菜谱")
    @GetMapping("/{id}")
    public ApiResponse<Recipe> getRecipeById(@PathVariable Long id) {
        return ApiResponse.success(recipeService.getRecipeById(id));
    }

    @OperationLog(action = "CREATE", target = "菜谱")
    @PostMapping
    public ApiResponse<Recipe> createRecipe(@Valid @RequestBody RecipeCreateRequest request) {
        return ApiResponse.success(recipeService.createRecipe(request));
    }

    @OperationLog(action = "UPDATE", target = "菜谱")
    @PutMapping("/{id}")
    public ApiResponse<Recipe> updateRecipe(@PathVariable Long id, @Valid @RequestBody RecipeUpdateRequest request) {
        return ApiResponse.success(recipeService.updateRecipe(id, request));
    }

    @OperationLog(action = "DELETE", target = "菜谱")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "菜谱")
    @PutMapping("/{id}/favorite")
    public ApiResponse<Void> toggleFavorite(@PathVariable Long id) {
        recipeService.updateFavorite(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "菜谱")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        recipeService.updateStatus(id, body.get("status"));
        return ApiResponse.success();
    }

    @OperationLog(action = "BATCH_DELETE", target = "菜谱")
    @PostMapping("/batch/delete")
    public ApiResponse<Void> batchDelete(@RequestBody BatchRequest request) {
        recipeService.batchDelete(request.getIds());
        return ApiResponse.success();
    }

    @OperationLog(action = "BATCH_UPDATE", target = "菜谱")
    @PostMapping("/batch/status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody BatchRequest request) {
        recipeService.batchUpdateStatus(request.getIds(), request.getStatus());
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "菜谱")
    @GetMapping("/categories")
    public ApiResponse<List<RecipeCategory>> getCategories() {
        return ApiResponse.success(recipeService.getCategories());
    }

    @OperationLog(action = "QUERY", target = "菜谱")
    @GetMapping("/random")
    public ApiResponse<List<Recipe>> getRandomRecipes(
            @RequestParam(defaultValue = "6") int count,
            @RequestParam(required = false) Long coupleId) {
        return ApiResponse.success(recipeService.getRandomRecipes(count, coupleId));
    }
}
