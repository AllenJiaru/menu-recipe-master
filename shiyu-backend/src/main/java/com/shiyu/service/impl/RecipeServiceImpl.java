package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.RecipeCreateRequest;
import com.shiyu.dto.request.RecipeUpdateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeCategory;
import com.shiyu.entity.RecipeMaterial;
import com.shiyu.entity.RecipeStep;
import com.shiyu.mapper.RecipeCategoryMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.RecipeMaterialMapper;
import com.shiyu.mapper.RecipeStepMapper;
import com.shiyu.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeMaterialMapper recipeMaterialMapper;

    @Autowired
    private RecipeStepMapper recipeStepMapper;

    @Autowired
    private RecipeCategoryMapper recipeCategoryMapper;

    @Override
    public PageResponse<Recipe> getRecipes(Integer page, Integer size, Long categoryId, String keyword, Integer difficulty, Long coupleId, Integer status) {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;

        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Recipe::getCategoryId, categoryId);
        wrapper.eq(difficulty != null, Recipe::getDifficulty, difficulty);
        wrapper.eq(coupleId != null, Recipe::getCoupleId, coupleId);
        wrapper.eq(status != null, Recipe::getStatus, status);
        wrapper.like(StringUtils.hasText(keyword), Recipe::getName, keyword);
        wrapper.orderByDesc(Recipe::getCreateTime);

        Page<Recipe> pageResult = recipeMapper.selectPage(new Page<>(page, size), wrapper);
        // 填充食材清单和烹饪步骤
        for (Recipe recipe : pageResult.getRecords()) {
            recipe.setMaterials(recipeMaterialMapper.findByRecipeId(recipe.getId()));
            recipe.setSteps(recipeStepMapper.findByRecipeId(recipe.getId()));
        }
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public Recipe getRecipeById(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }
        recipe.setMaterials(recipeMaterialMapper.findByRecipeId(id));
        recipe.setSteps(recipeStepMapper.findByRecipeId(id));
        return recipe;
    }

    @Override
    @Transactional
    public Recipe createRecipe(RecipeCreateRequest request) {
        Recipe recipe = new Recipe();
        recipe.setCoupleId(request.getCoupleId());
        recipe.setName(request.getName());
        recipe.setCategoryId(request.getCategoryId());
        recipe.setType(request.getType());
        recipe.setDescription(request.getDescription());
        recipe.setCoverImage(request.getCoverImage());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setIsFavorite(0);
        recipe.setOrderCount(0);
        recipe.setStatus(1);
        recipe.setSyncId(UUID.randomUUID().toString());
        recipe.setSyncTime(LocalDateTime.now());
        recipeMapper.insert(recipe);

        saveMaterials(recipe.getId(), request.getMaterials());
        saveSteps(recipe.getId(), request.getSteps());

        return getRecipeById(recipe.getId());
    }

    @Override
    @Transactional
    public Recipe updateRecipe(Long id, RecipeUpdateRequest request) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }

        recipe.setCoupleId(request.getCoupleId());
        recipe.setName(request.getName());
        recipe.setCategoryId(request.getCategoryId());
        recipe.setType(request.getType());
        recipe.setDescription(request.getDescription());
        recipe.setCoverImage(request.getCoverImage());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setSyncTime(LocalDateTime.now());
        recipeMapper.updateById(recipe);

        recipeMaterialMapper.deleteByRecipeId(id);
        recipeStepMapper.deleteByRecipeId(id);
        saveMaterialsUpdate(id, request.getMaterials());
        saveStepsUpdate(id, request.getSteps());

        return getRecipeById(id);
    }

    @Override
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }
        recipeMapper.deleteById(id);
    }

    @Override
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        recipeMapper.deleteBatchIds(ids);
    }

    @Override
    public void batchUpdateStatus(java.util.List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty() || status == null) {
            return;
        }
        for (Long id : ids) {
            Recipe recipe = recipeMapper.selectById(id);
            if (recipe != null) {
                recipe.setStatus(status);
                recipeMapper.updateById(recipe);
            }
        }
    }

    @Override
    public void updateFavorite(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }
        recipe.setIsFavorite(recipe.getIsFavorite() == null || recipe.getIsFavorite() == 0 ? 1 : 0);
        recipeMapper.updateById(recipe);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }
        recipe.setStatus(status);
        recipeMapper.updateById(recipe);
    }

    @Override
    public List<RecipeCategory> getCategories() {
        LambdaQueryWrapper<RecipeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RecipeCategory::getSortOrder);
        return recipeCategoryMapper.selectList(wrapper);
    }

    @Override
    public List<Recipe> getRandomRecipes(Integer count, Long coupleId) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(coupleId != null, Recipe::getCoupleId, coupleId);
        wrapper.eq(Recipe::getStatus, 1);
        List<Recipe> all = recipeMapper.selectList(wrapper);
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.shuffle(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    private void saveMaterials(Long recipeId, List<RecipeCreateRequest.MaterialRequest> materialRequests) {
        if (materialRequests == null || materialRequests.isEmpty()) {
            return;
        }
        for (RecipeCreateRequest.MaterialRequest mr : materialRequests) {
            RecipeMaterial material = new RecipeMaterial();
            material.setRecipeId(recipeId);
            material.setName(mr.getName());
            material.setAmount(mr.getAmount());
            material.setUnit(mr.getUnit());
            material.setSortOrder(mr.getSortOrder());
            material.setSyncId(UUID.randomUUID().toString());
            recipeMaterialMapper.insert(material);
        }
    }

    private void saveSteps(Long recipeId, List<RecipeCreateRequest.StepRequest> stepRequests) {
        if (stepRequests == null || stepRequests.isEmpty()) {
            return;
        }
        for (RecipeCreateRequest.StepRequest sr : stepRequests) {
            RecipeStep step = new RecipeStep();
            step.setRecipeId(recipeId);
            step.setStepNumber(sr.getStepNumber());
            step.setDescription(sr.getDescription());
            step.setImageUrl(sr.getImageUrl());
            step.setSyncId(UUID.randomUUID().toString());
            recipeStepMapper.insert(step);
        }
    }

    private void saveMaterialsUpdate(Long recipeId, List<RecipeUpdateRequest.MaterialRequest> materialRequests) {
        if (materialRequests == null || materialRequests.isEmpty()) {
            return;
        }
        for (RecipeUpdateRequest.MaterialRequest mr : materialRequests) {
            RecipeMaterial material = new RecipeMaterial();
            material.setRecipeId(recipeId);
            material.setName(mr.getName());
            material.setAmount(mr.getAmount());
            material.setUnit(mr.getUnit());
            material.setSortOrder(mr.getSortOrder());
            material.setSyncId(UUID.randomUUID().toString());
            recipeMaterialMapper.insert(material);
        }
    }

    private void saveStepsUpdate(Long recipeId, List<RecipeUpdateRequest.StepRequest> stepRequests) {
        if (stepRequests == null || stepRequests.isEmpty()) {
            return;
        }
        for (RecipeUpdateRequest.StepRequest sr : stepRequests) {
            RecipeStep step = new RecipeStep();
            step.setRecipeId(recipeId);
            step.setStepNumber(sr.getStepNumber());
            step.setDescription(sr.getDescription());
            step.setImageUrl(sr.getImageUrl());
            step.setSyncId(UUID.randomUUID().toString());
            recipeStepMapper.insert(step);
        }
    }
}
