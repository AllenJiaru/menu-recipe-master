package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.dto.response.StatisticsResponse;
import com.shiyu.entity.OrderRecord;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeCategory;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeCategoryMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeCategoryMapper recipeCategoryMapper;

    @Override
    public StatisticsResponse getOrderStats(Long coupleId, String startDate, String endDate) {
        StatisticsResponse response = new StatisticsResponse();

        LocalDate startLocal = StringUtils.hasText(startDate) ? LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().minusDays(30);
        LocalDate endLocal = StringUtils.hasText(endDate) ? LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now();

        List<String> labels = new ArrayList<>();
        List<Long> orderData = new ArrayList<>();

        for (LocalDate date = startLocal; !date.isAfter(endLocal); date = date.plusDays(1)) {
            labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(coupleId != null, OrderRecord::getCoupleId, coupleId);
            wrapper.ge(OrderRecord::getOrderTime, dayStart);
            wrapper.le(OrderRecord::getOrderTime, dayEnd);
            orderData.add(orderRecordMapper.selectCount(wrapper));
        }
        response.setLabels(labels);
        response.setOrderData(orderData);

        Map<String, Long> categoryStats = new LinkedHashMap<>();
        LambdaQueryWrapper<Recipe> recipeWrapper = new LambdaQueryWrapper<>();
        recipeWrapper.eq(coupleId != null, Recipe::getCoupleId, coupleId);
        List<Recipe> recipes = recipeMapper.selectList(recipeWrapper);

        List<RecipeCategory> categories = recipeCategoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(RecipeCategory::getId, RecipeCategory::getName));

        Map<Long, Long> categoryCount = recipes.stream()
                .filter(r -> r.getCategoryId() != null)
                .collect(Collectors.groupingBy(Recipe::getCategoryId, Collectors.counting()));

        categoryCount.forEach((catId, count) -> {
            String catName = categoryNameMap.getOrDefault(catId, "Unknown");
            categoryStats.put(catName, count);
        });
        response.setCategoryStats(categoryStats);

        LambdaQueryWrapper<Recipe> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(coupleId != null, Recipe::getCoupleId, coupleId);
        topWrapper.orderByDesc(Recipe::getOrderCount);
        topWrapper.last("LIMIT 10");
        List<Recipe> topRecipes = recipeMapper.selectList(topWrapper);
        List<StatisticsResponse.RecipeStat> topList = topRecipes.stream().map(r -> {
            StatisticsResponse.RecipeStat stat = new StatisticsResponse.RecipeStat();
            stat.setId(r.getId());
            stat.setName(r.getName());
            stat.setOrderCount(r.getOrderCount() != null ? r.getOrderCount().longValue() : 0L);
            return stat;
        }).collect(Collectors.toList());
        response.setTopRecipes(topList);

        return response;
    }
}
