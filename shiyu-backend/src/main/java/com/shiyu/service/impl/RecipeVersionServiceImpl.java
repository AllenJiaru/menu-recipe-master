package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.BusinessException;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeMaterial;
import com.shiyu.entity.RecipeStep;
import com.shiyu.entity.RecipeVersion;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.RecipeMaterialMapper;
import com.shiyu.mapper.RecipeStepMapper;
import com.shiyu.mapper.RecipeVersionMapper;
import com.shiyu.service.RecipeVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecipeVersionServiceImpl implements RecipeVersionService {

    @Autowired
    private RecipeVersionMapper recipeVersionMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeMaterialMapper recipeMaterialMapper;

    @Autowired
    private RecipeStepMapper recipeStepMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveVersion(Long recipeId, String changeNote, Long operatorId, String operatorName) {
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }

        LambdaQueryWrapper<RecipeVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeVersion::getRecipeId, recipeId);
        Long count = recipeVersionMapper.selectCount(wrapper);
        int nextVersion = count.intValue() + 1;

        List<RecipeMaterial> materials = recipeMaterialMapper.findByRecipeId(recipeId);
        List<RecipeStep> steps = recipeStepMapper.findByRecipeId(recipeId);

        String materialsJson = "[]";
        String stepsJson = "[]";
        try {
            materialsJson = objectMapper.writeValueAsString(materials);
            stepsJson = objectMapper.writeValueAsString(steps);
        } catch (Exception e) {
            // use empty JSON arrays as fallback
        }

        RecipeVersion version = new RecipeVersion();
        version.setRecipeId(recipeId);
        version.setVersion(nextVersion);
        version.setName(recipe.getName());
        version.setCover(recipe.getCoverImage());
        version.setDescription(recipe.getDescription());
        version.setDifficulty(recipe.getDifficulty() != null ? String.valueOf(recipe.getDifficulty()) : null);
        version.setCookingTime(recipe.getCookingTime());
        version.setMaterialsJson(materialsJson);
        version.setStepsJson(stepsJson);
        version.setChangeNote(changeNote);
        version.setOperatorId(operatorId);
        version.setOperatorName(operatorName);
        version.setCreateTime(LocalDateTime.now());
        recipeVersionMapper.insert(version);
    }

    @Override
    public List<RecipeVersion> getVersionHistory(Long recipeId) {
        return recipeVersionMapper.findByRecipe(recipeId);
    }

    @Override
    public RecipeVersion getVersion(Long id) {
        RecipeVersion version = recipeVersionMapper.selectById(id);
        if (version == null) {
            throw new BusinessException("Version not found");
        }
        return version;
    }

    @Override
    public void restoreVersion(Long versionId, Long operatorId, String operatorName) {
        RecipeVersion version = recipeVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("Version not found");
        }

        Recipe recipe = recipeMapper.selectById(version.getRecipeId());
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }

        recipe.setName(version.getName());
        recipe.setCoverImage(version.getCover());
        recipe.setDescription(version.getDescription());
        if (version.getDifficulty() != null) {
            try {
                recipe.setDifficulty(Integer.parseInt(version.getDifficulty()));
            } catch (NumberFormatException e) {
                // skip if difficulty is not a valid integer
            }
        }
        recipe.setCookingTime(version.getCookingTime());
        recipeMapper.updateById(recipe);

        saveVersion(version.getRecipeId(), "Restore from version " + version.getVersion(), operatorId, operatorName);
    }
}
