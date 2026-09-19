package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.dto.response.DashboardResponse;
import com.shiyu.entity.OrderRecord;
import com.shiyu.entity.Recipe;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Override
    public DashboardResponse getOverview() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalRecipes(recipeMapper.selectCount(null));
        response.setTotalOrders(orderRecordMapper.selectCount(null));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<OrderRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(OrderRecord::getOrderTime, todayStart);
        todayWrapper.le(OrderRecord::getOrderTime, todayEnd);
        response.setTodayOrders(orderRecordMapper.selectCount(todayWrapper));

        LambdaQueryWrapper<OrderRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(OrderRecord::getStatus, 0);
        response.setPendingOrders(orderRecordMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<OrderRecord> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(OrderRecord::getStatus, 3);
        response.setCompletedOrders(orderRecordMapper.selectCount(completedWrapper));

        LambdaQueryWrapper<OrderRecord> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.orderByDesc(OrderRecord::getOrderTime);
        recentWrapper.last("LIMIT 5");
        List<OrderRecord> recentOrders = orderRecordMapper.selectList(recentWrapper);
        List<DashboardResponse.RecentOrder> recentList = recentOrders.stream().map(o -> {
            DashboardResponse.RecentOrder ro = new DashboardResponse.RecentOrder();
            ro.setId(o.getId());
            ro.setRecipeName(o.getRecipeName());
            ro.setStatus(o.getStatus());
            ro.setOrderTime(o.getOrderTime() != null ? o.getOrderTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            return ro;
        }).collect(Collectors.toList());
        response.setRecentOrders(recentList);

        LambdaQueryWrapper<Recipe> popularWrapper = new LambdaQueryWrapper<>();
        popularWrapper.orderByDesc(Recipe::getOrderCount);
        popularWrapper.last("LIMIT 5");
        List<Recipe> popularRecipes = recipeMapper.selectList(popularWrapper);
        List<DashboardResponse.PopularRecipe> popularList = popularRecipes.stream().map(r -> {
            DashboardResponse.PopularRecipe pr = new DashboardResponse.PopularRecipe();
            pr.setId(r.getId());
            pr.setName(r.getName());
            pr.setOrderCount(r.getOrderCount());
            pr.setCoverImage(r.getCoverImage());
            return pr;
        }).collect(Collectors.toList());
        response.setPopularRecipes(popularList);

        List<String> trendLabels = new ArrayList<>();
        List<Long> trendOrderData = new ArrayList<>();
        List<Long> trendRecipeData = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            trendLabels.add(date.format(labelFormatter));

            LambdaQueryWrapper<OrderRecord> orderDayWrapper = new LambdaQueryWrapper<>();
            orderDayWrapper.ge(OrderRecord::getOrderTime, dayStart);
            orderDayWrapper.le(OrderRecord::getOrderTime, dayEnd);
            trendOrderData.add(orderRecordMapper.selectCount(orderDayWrapper));

            LambdaQueryWrapper<Recipe> recipeDayWrapper = new LambdaQueryWrapper<>();
            recipeDayWrapper.ge(Recipe::getCreateTime, dayStart);
            recipeDayWrapper.le(Recipe::getCreateTime, dayEnd);
            trendRecipeData.add(recipeMapper.selectCount(recipeDayWrapper));
        }
        response.setTrendLabels(trendLabels);
        response.setTrendOrderData(trendOrderData);
        response.setTrendRecipeData(trendRecipeData);

        return response;
    }
}
