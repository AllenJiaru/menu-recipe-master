package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import com.shiyu.dto.request.MealPlanRequest;
import com.shiyu.entity.MealPlan;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.MealPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    @Autowired
    private MealPlanService mealPlanService;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @OperationLog(action = "QUERY", target = "周菜谱")
    @GetMapping("/weekly")
    @PreAuthorize("hasAuthority('business:mealplan')")
    public ApiResponse<List<MealPlan>> getWeeklyPlan(@RequestParam String startDate) {
        User user = getCurrentUser();
        return ApiResponse.success(mealPlanService.getWeeklyPlan(user.getId(), startDate));
    }

    @OperationLog(action = "CREATE", target = "周菜谱")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('business:mealplan:create','business:mealplan:edit')")
    public ApiResponse<Void> savePlan(@Valid @RequestBody MealPlanRequest request) {
        User user = getCurrentUser();
        mealPlanService.savePlan(request, user.getId());
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "周菜谱")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:mealplan:delete')")
    public ApiResponse<Void> deletePlan(@PathVariable Long id) {
        mealPlanService.deletePlan(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "周菜谱")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('business:mealplan')")
    public ApiResponse<Map<String, Object>> getPlanStats() {
        User user = getCurrentUser();
        return ApiResponse.success(mealPlanService.getPlanStats(user.getId()));
    }
}
