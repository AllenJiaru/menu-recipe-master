package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.CommentRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeComment;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.RecipeCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class RecipeCommentController {

    @Autowired
    private RecipeCommentService recipeCommentService;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @GetMapping("/recipe/{recipeId}")
    public ApiResponse<PageResponse<RecipeComment>> getComments(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(recipeCommentService.getComments(recipeId, page, size));
    }

    @PostMapping
    public ApiResponse<Void> addComment(@Valid @RequestBody CommentRequest request) {
        User user = getCurrentUser();
        recipeCommentService.addComment(request, user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        User user = getCurrentUser();
        recipeCommentService.deleteComment(id, user.getId());
        return ApiResponse.success();
    }

    @GetMapping("/stats/{recipeId}")
    public ApiResponse<Map<String, Object>> getCommentStats(@PathVariable Long recipeId) {
        return ApiResponse.success(recipeCommentService.getCommentStats(recipeId));
    }
}
