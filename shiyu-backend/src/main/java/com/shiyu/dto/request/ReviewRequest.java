package com.shiyu.dto.request;

import jakarta.validation.constraints.NotNull;

public class ReviewRequest {

    private Long recipeId;

    @NotNull(message = "status must not be null")
    private String status;

    private String comment;

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusValue() {
        if (status == null) {
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
                    return 0;
                }
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
