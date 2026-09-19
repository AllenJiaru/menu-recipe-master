package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.CategoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeCategory;
import com.shiyu.mapper.RecipeCategoryMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private RecipeCategoryMapper recipeCategoryMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Override
    public PageResponse<RecipeCategory> getCategories(int page, int size) {
        LambdaQueryWrapper<RecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RecipeCategory::getSortOrder);

        Page<RecipeCategory> pageResult = recipeCategoryMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public RecipeCategory getCategoryById(Long id) {
        RecipeCategory category = recipeCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("Category not found");
        }
        return category;
    }

    @Override
    public void createCategory(CategoryRequest request) {
        LambdaQueryWrapper<RecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeCategory::getName, request.getName());
        Long count = recipeCategoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("Category name already exists");
        }

        RecipeCategory category = new RecipeCategory();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        recipeCategoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, CategoryRequest request) {
        RecipeCategory existing = recipeCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Category not found");
        }

        LambdaQueryWrapper<RecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeCategory::getName, request.getName());
        wrapper.ne(RecipeCategory::getId, id);
        Long count = recipeCategoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("Category name already exists");
        }

        existing.setName(request.getName());
        existing.setIcon(request.getIcon());
        existing.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        recipeCategoryMapper.updateById(existing);
    }

    @Override
    public void deleteCategory(Long id) {
        RecipeCategory existing = recipeCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Category not found");
        }

        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getCategoryId, id);
        Long count = recipeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("Cannot delete category: recipes are using it");
        }

        recipeCategoryMapper.deleteById(id);
    }
}
