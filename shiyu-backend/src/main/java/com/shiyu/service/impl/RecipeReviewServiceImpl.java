package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.ReviewRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeReview;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.RecipeReviewMapper;
import com.shiyu.service.RecipeReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecipeReviewServiceImpl implements RecipeReviewService {

    @Autowired
    private RecipeReviewMapper recipeReviewMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Override
    public PageResponse<RecipeReview> getReviews(int page, int size, Integer status) {
        LambdaQueryWrapper<RecipeReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, RecipeReview::getStatus, status);
        wrapper.orderByDesc(RecipeReview::getCreateTime);

        Page<RecipeReview> pageResult = recipeReviewMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public void createReview(RecipeReview review) {
        LambdaQueryWrapper<RecipeReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeReview::getRecipeId, review.getRecipeId());
        wrapper.eq(RecipeReview::getStatus, 0);
        Long count = recipeReviewMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("This recipe already has a pending review");
        }

        review.setStatus(review.getStatus() != null ? review.getStatus() : 0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        recipeReviewMapper.insert(review);

        // Mark recipe as pending review (status = 2)
        if (review.getRecipeId() != null) {
            Recipe recipe = recipeMapper.selectById(review.getRecipeId());
            if (recipe != null) {
                recipe.setStatus(2);
                recipeMapper.updateById(recipe);
            }
        }
    }

    @Override
    public void updateReview(Long id, ReviewRequest request, String reviewerName) {
        RecipeReview existing = recipeReviewMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Review not found");
        }

        existing.setStatus(request.getStatusValue());
        existing.setComment(request.getComment());
        existing.setReviewerName(reviewerName);
        existing.setReviewTime(LocalDateTime.now());
        existing.setUpdateTime(LocalDateTime.now());
        recipeReviewMapper.updateById(existing);

        applyToRecipe(existing.getRecipeId(), existing.getStatus());
    }

    @Override
    public void batchAudit(java.util.List<Long> ids, Integer status, String reviewerName, String comment) {
        if (ids == null || ids.isEmpty() || status == null) {
            return;
        }
        for (Long id : ids) {
            RecipeReview existing = recipeReviewMapper.selectById(id);
            if (existing == null) {
                continue;
            }
            existing.setStatus(status);
            existing.setComment(comment);
            existing.setReviewerName(reviewerName);
            existing.setReviewTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());
            recipeReviewMapper.updateById(existing);
            applyToRecipe(existing.getRecipeId(), status);
        }
    }

    private void applyToRecipe(Long recipeId, Integer status) {
        if (recipeId == null || status == null) {
            return;
        }
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            return;
        }
        if (status == 1) {
            recipe.setStatus(1); // approved -> published
        } else if (status == 2) {
            recipe.setStatus(0); // rejected -> unpublished
        }
        recipeMapper.updateById(recipe);
    }

    @Override
    public void deleteReview(Long id) {
        RecipeReview existing = recipeReviewMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Review not found");
        }
        recipeReviewMapper.deleteById(id);
    }

    @Override
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        recipeReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public List<RecipeReview> getPendingReviews() {
        return recipeReviewMapper.findPending();
    }
}
