package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.CategoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeCategory;
import com.shiyu.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @OperationLog(action = "QUERY", target = "分类")
    @GetMapping
    public ApiResponse<PageResponse<RecipeCategory>> getCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(categoryService.getCategories(page, size));
    }

    @OperationLog(action = "QUERY", target = "分类")
    @GetMapping("/{id}")
    public ApiResponse<RecipeCategory> getCategoryById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getCategoryById(id));
    }

    @OperationLog(action = "CREATE", target = "分类")
    @PostMapping
    public ApiResponse<Void> createCategory(@Valid @RequestBody CategoryRequest request) {
        categoryService.createCategory(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "分类")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success();
    }
}
