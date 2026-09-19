package com.shiyu.service;

import com.shiyu.dto.request.CategoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeCategory;

public interface CategoryService {
    PageResponse<RecipeCategory> getCategories(int page, int size);
    RecipeCategory getCategoryById(Long id);
    void createCategory(CategoryRequest request);
    void updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
