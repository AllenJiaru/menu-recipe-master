package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.entity.RecipeVersion;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.RecipeVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe-versions")
public class RecipeVersionController {

    @Autowired
    private RecipeVersionService recipeVersionService;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @PostMapping("/{recipeId}")
    public ApiResponse<Void> saveVersion(@PathVariable Long recipeId, @RequestParam(required = false) String changeNote) {
        User user = getCurrentUser();
        recipeVersionService.saveVersion(recipeId, changeNote, user.getId(), user.getNickname());
        return ApiResponse.success();
    }

    @GetMapping("/{recipeId}")
    public ApiResponse<List<RecipeVersion>> getVersionHistory(@PathVariable Long recipeId) {
        return ApiResponse.success(recipeVersionService.getVersionHistory(recipeId));
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<RecipeVersion> getVersionDetail(@PathVariable Long id) {
        return ApiResponse.success(recipeVersionService.getVersion(id));
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restoreVersion(@PathVariable Long id) {
        User user = getCurrentUser();
        recipeVersionService.restoreVersion(id, user.getId(), user.getNickname());
        return ApiResponse.success();
    }
}
