package com.shiyu.service;

import com.shiyu.dto.request.MealPlanRequest;
import com.shiyu.entity.MealPlan;

import java.util.List;
import java.util.Map;

public interface MealPlanService {
    List<MealPlan> getWeeklyPlan(Long userId, String startDate);
    void savePlan(MealPlanRequest request, Long userId);
    void deletePlan(Long id);
    Map<String, Object> getPlanStats(Long userId);
}
