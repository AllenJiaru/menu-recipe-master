package com.shiyu.dto.response;

import java.util.List;
import java.util.Map;

public class StatisticsResponse {
    private List<String> labels;
    private List<Long> orderData;
    private Map<String, Long> categoryStats;
    private List<RecipeStat> topRecipes;

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getOrderData() {
        return orderData;
    }

    public void setOrderData(List<Long> orderData) {
        this.orderData = orderData;
    }

    public Map<String, Long> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(Map<String, Long> categoryStats) {
        this.categoryStats = categoryStats;
    }

    public List<RecipeStat> getTopRecipes() {
        return topRecipes;
    }

    public void setTopRecipes(List<RecipeStat> topRecipes) {
        this.topRecipes = topRecipes;
    }

    public static class RecipeStat {
        private Long id;
        private String name;
        private Long orderCount;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Long orderCount) {
            this.orderCount = orderCount;
        }
    }
}