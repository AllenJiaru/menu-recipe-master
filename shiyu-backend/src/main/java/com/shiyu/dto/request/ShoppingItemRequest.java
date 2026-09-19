package com.shiyu.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ShoppingItemRequest {

    private String listName;

    @NotBlank(message = "ingredientName must not be blank")
    private String ingredientName;

    private Double quantity;

    private String unit;

    private String category;

    private Long sourceRecipeId;

    private String sourceRecipeName;

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getSourceRecipeId() {
        return sourceRecipeId;
    }

    public void setSourceRecipeId(Long sourceRecipeId) {
        this.sourceRecipeId = sourceRecipeId;
    }

    public String getSourceRecipeName() {
        return sourceRecipeName;
    }

    public void setSourceRecipeName(String sourceRecipeName) {
        this.sourceRecipeName = sourceRecipeName;
    }
}
