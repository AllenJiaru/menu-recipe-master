package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.BatchRequest;
import com.shiyu.dto.request.ReviewRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeReview;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.RecipeReviewService;
import com.shiyu.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class RecipeReviewController {

    @Autowired
    private RecipeReviewService recipeReviewService;

    @Autowired
    private RecipeService recipeService;

    private String currentReviewerName() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userDetails.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private Long currentReviewerId() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userDetails.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ApiResponse<PageResponse<RecipeReview>> getReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(recipeReviewService.getReviews(page, size, parseStatus(status)));
    }

    private Integer parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        switch (status) {
            case "approved":
                return 1;
            case "rejected":
                return 2;
            case "pending":
                return 0;
            default:
                try {
                    return Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    return null;
                }
        }
    }

    @PostMapping
    public ApiResponse<Void> createReview(@Valid @RequestBody ReviewRequest request) {
        buildAndCreate(request.getRecipeId(), request.getStatusValue(), request.getComment());
        return ApiResponse.success();
    }

    @PostMapping("/batch")
    public ApiResponse<Void> batchCreateReviews(@RequestBody BatchRequest request) {
        if (request.getIds() == null) {
            return ApiResponse.success();
        }
        int success = 0;
        for (Long recipeId : request.getIds()) {
            try {
                buildAndCreate(recipeId, 0, "批量提交审核");
                success++;
            } catch (Exception ignored) {
            }
        }
        return ApiResponse.success();
    }

    @PostMapping("/batch/audit")
    public ApiResponse<Void> batchAudit(@RequestBody BatchRequest request) {
        recipeReviewService.batchAudit(request.getIds(), request.getStatus(), currentReviewerName(), request.getComment());
        return ApiResponse.success();
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> batchDelete(@RequestBody BatchRequest request) {
        recipeReviewService.batchDelete(request.getIds());
        return ApiResponse.success();
    }

    private void buildAndCreate(Long recipeId, Integer status, String comment) {
        RecipeReview review = new RecipeReview();
        review.setRecipeId(recipeId);
        review.setStatus(status != null ? status : 0);
        review.setComment(comment);
        if (recipeId != null) {
            try {
                Recipe recipe = recipeService.getRecipeById(recipeId);
                if (recipe != null) {
                    review.setRecipeName(recipe.getName());
                }
            } catch (Exception ignored) {
            }
        }
        if (review.getRecipeName() == null) {
            review.setRecipeName("未命名菜谱");
        }
        review.setReviewerId(currentReviewerId() != null ? currentReviewerId() : 0L);
        review.setReviewerName(currentReviewerName());
        recipeReviewService.createReview(review);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        recipeReviewService.updateReview(id, request, currentReviewerName());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        recipeReviewService.deleteReview(id);
        return ApiResponse.success();
    }

    @GetMapping("/pending")
    public ApiResponse<List<RecipeReview>> getPendingReviews() {
        return ApiResponse.success(recipeReviewService.getPendingReviews());
    }
}
