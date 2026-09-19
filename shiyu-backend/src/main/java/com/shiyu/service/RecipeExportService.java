package com.shiyu.service;

public interface RecipeExportService {
    byte[] exportRecipes(String format, String startDate, String endDate);
    byte[] exportOrders(String format, String startDate, String endDate);
}
