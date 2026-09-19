package com.shiyu.service;

import com.shiyu.dto.request.ReviewRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.RecipeReview;
import java.util.List;

public interface RecipeReviewService {
    PageResponse<RecipeReview> getReviews(int page, int size, Integer status);
    void createReview(RecipeReview review);
    void updateReview(Long id, ReviewRequest request, String reviewerName);
    void deleteReview(Long id);
    void batchAudit(java.util.List<Long> ids, Integer status, String reviewerName, String comment);
    void batchDelete(java.util.List<Long> ids);
    List<RecipeReview> getPendingReviews();
}
