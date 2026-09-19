package com.shiyu.service;

import com.shiyu.entity.RecipeVersion;

import java.util.List;

public interface RecipeVersionService {
    void saveVersion(Long recipeId, String changeNote, Long operatorId, String operatorName);
    List<RecipeVersion> getVersionHistory(Long recipeId);
    RecipeVersion getVersion(Long id);
    void restoreVersion(Long versionId, Long operatorId, String operatorName);
}
