package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.MealPlanRequest;
import com.shiyu.entity.MealPlan;
import com.shiyu.mapper.MealPlanMapper;
import com.shiyu.service.MealPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MealPlanServiceImpl implements MealPlanService {

    @Autowired
    private MealPlanMapper mealPlanMapper;

    @Override
    public List<MealPlan> getWeeklyPlan(Long userId, String startDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = start.plusDays(6);
        return mealPlanMapper.findByDateRange(userId, start.toString(), end.toString());
    }

    @Override
    public void savePlan(MealPlanRequest request, Long userId) {
        MealPlan plan = new MealPlan();
        plan.setUserId(userId);
        plan.setPlanDate(LocalDate.parse(request.getPlanDate()));
        plan.setMealType(request.getMealType());
        plan.setRecipeId(request.getRecipeId());
        plan.setRecipeName(request.getRecipeName());
        plan.setRecipeCover(request.getRecipeCover());
        plan.setServings(request.getServings());
        plan.setNote(request.getNote());
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setDeleted(0);
        mealPlanMapper.insert(plan);
    }

    @Override
    public void deletePlan(Long id) {
        MealPlan existing = mealPlanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Meal plan not found");
        }
        mealPlanMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getPlanStats(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<MealPlan> weeklyPlans = mealPlanMapper.findByDateRange(userId, weekStart.toString(), weekEnd.toString());

        Map<String, Integer> mealTypeCount = new HashMap<>();
        for (MealPlan plan : weeklyPlans) {
            String type = plan.getMealType();
            mealTypeCount.put(type, mealTypeCount.getOrDefault(type, 0) + 1);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("weeklyTotal", weeklyPlans.size());
        stats.put("mealTypeCount", mealTypeCount);
        return stats;
    }
}
