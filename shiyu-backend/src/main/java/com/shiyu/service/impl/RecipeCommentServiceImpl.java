package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.CommentRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeComment;
import com.shiyu.mapper.RecipeCommentMapper;
import com.shiyu.service.RecipeCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RecipeCommentServiceImpl implements RecipeCommentService {

    @Autowired
    private RecipeCommentMapper recipeCommentMapper;

    @Override
    public PageResponse<RecipeComment> getComments(Long recipeId, int page, int size) {
        LambdaQueryWrapper<RecipeComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeComment::getRecipeId, recipeId);
        wrapper.eq(RecipeComment::getParentId, 0);
        wrapper.orderByDesc(RecipeComment::getCreateTime);

        Page<RecipeComment> pageResult = recipeCommentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public void addComment(CommentRequest request, Long userId, String username, String nickname, String avatar) {
        RecipeComment comment = new RecipeComment();
        comment.setRecipeId(request.getRecipeId());
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setNickname(nickname);
        comment.setAvatar(avatar);
        comment.setContent(request.getContent());
        comment.setRating(request.getRating());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setDeleted(0);
        recipeCommentMapper.insert(comment);
    }

    @Override
    public void deleteComment(Long id, Long userId) {
        RecipeComment comment = recipeCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("No permission to delete this comment");
        }
        recipeCommentMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getCommentStats(Long recipeId) {
        LambdaQueryWrapper<RecipeComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeComment::getRecipeId, recipeId);
        Long totalComments = recipeCommentMapper.selectCount(wrapper);

        LambdaQueryWrapper<RecipeComment> ratingWrapper = new LambdaQueryWrapper<>();
        ratingWrapper.eq(RecipeComment::getRecipeId, recipeId);
        ratingWrapper.isNotNull(RecipeComment::getRating);
        Long ratedCount = recipeCommentMapper.selectCount(ratingWrapper);

        double avgRating = 0;
        if (ratedCount > 0) {
            LambdaQueryWrapper<RecipeComment> sumWrapper = new LambdaQueryWrapper<>();
            sumWrapper.eq(RecipeComment::getRecipeId, recipeId);
            sumWrapper.isNotNull(RecipeComment::getRating);
            java.util.List<RecipeComment> ratedComments = recipeCommentMapper.selectList(sumWrapper);
            double sum = 0;
            for (RecipeComment c : ratedComments) {
                if (c.getRating() != null) {
                    sum += c.getRating();
                }
            }
            avgRating = sum / ratedCount;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", totalComments);
        stats.put("ratedCount", ratedCount);
        stats.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
        return stats;
    }
}
