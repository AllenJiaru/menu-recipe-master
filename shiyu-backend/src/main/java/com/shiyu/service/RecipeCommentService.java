package com.shiyu.service;

import com.shiyu.dto.request.CommentRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeComment;

import java.util.Map;

public interface RecipeCommentService {
    PageResponse<RecipeComment> getComments(Long recipeId, int page, int size);
    void addComment(CommentRequest request, Long userId, String username, String nickname, String avatar);
    void deleteComment(Long id, Long userId);
    Map<String, Object> getCommentStats(Long recipeId);
}
