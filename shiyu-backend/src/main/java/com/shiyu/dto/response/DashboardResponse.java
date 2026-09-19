package com.shiyu.dto.response;

import java.util.List;

public class DashboardResponse {
    private Long totalRecipes;
    private Long totalOrders;
    private Long todayOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private List<RecentOrder> recentOrders;
    private List<PopularRecipe> popularRecipes;
    private List<String> trendLabels;
    private List<Long> trendOrderData;
    private List<Long> trendRecipeData;

    public Long getTotalRecipes() {
        return totalRecipes;
    }

    public void setTotalRecipes(Long totalRecipes) {
        this.totalRecipes = totalRecipes;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public List<RecentOrder> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrder> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<PopularRecipe> getPopularRecipes() {
        return popularRecipes;
    }

    public void setPopularRecipes(List<PopularRecipe> popularRecipes) {
        this.popularRecipes = popularRecipes;
    }

    public List<String> getTrendLabels() {
        return trendLabels;
    }

    public void setTrendLabels(List<String> trendLabels) {
        this.trendLabels = trendLabels;
    }

    public List<Long> getTrendOrderData() {
        return trendOrderData;
    }

    public void setTrendOrderData(List<Long> trendOrderData) {
        this.trendOrderData = trendOrderData;
    }

    public List<Long> getTrendRecipeData() {
        return trendRecipeData;
    }

    public void setTrendRecipeData(List<Long> trendRecipeData) {
        this.trendRecipeData = trendRecipeData;
    }

    public static class RecentOrder {
        private Long id;
        private String recipeName;
        private Integer status;
        private String orderTime;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getRecipeName() {
            return recipeName;
        }

        public void setRecipeName(String recipeName) {
            this.recipeName = recipeName;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(String orderTime) {
            this.orderTime = orderTime;
        }
    }

    public static class PopularRecipe {
        private Long id;
        private String name;
        private Integer orderCount;
        private String coverImage;

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

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }
    }
}