package com.shiyu.dto.request;

import java.util.List;

public class AiRecipeRequest {
    private List<String> ingredients;
    private String taste;
    private String cuisine;
    private Integer servings;
    private Integer maxTime;
    private String difficulty;
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public String getTaste() { return taste; }
    public void setTaste(String taste) { this.taste = taste; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    public Integer getMaxTime() { return maxTime; }
    public void setMaxTime(Integer maxTime) { this.maxTime = maxTime; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}