package com.shiyu.dto.request;

import java.util.List;

public class AiMealPlanRequest {
    private Integer days;
    private Double budget;
    private List<String> preferences;
    private List<String> allergies;
    private String style;
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
}