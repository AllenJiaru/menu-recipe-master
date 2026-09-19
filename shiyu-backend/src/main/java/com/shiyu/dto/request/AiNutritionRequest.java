package com.shiyu.dto.request;

import java.util.List;

public class AiNutritionRequest {
    private String recipeName;
    private List<String> ingredients;
    private String servings;
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public String getServings() { return servings; }
    public void setServings(String servings) { this.servings = servings; }
}