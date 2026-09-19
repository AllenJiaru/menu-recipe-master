package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.OrderCreateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.dto.response.StatisticsResponse;
import com.shiyu.entity.OrderRecord;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.User;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Override
    public PageResponse<OrderRecord> getOrders(Integer page, Integer size, Integer status, Long coupleId, String startDate, String endDate) {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;

        LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, OrderRecord::getStatus, status);
        wrapper.eq(coupleId != null, OrderRecord::getCoupleId, coupleId);

        // 食客只能看到自己的订单；主厨/管理员看到全部
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
                User user = userDetails.getUser();
                if (user != null && "diner".equals(user.getRole())) {
                    wrapper.eq(OrderRecord::getUserId, user.getId());
                }
            }
        } catch (Exception ignored) {
        }

        if (StringUtils.hasText(startDate)) {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            wrapper.ge(OrderRecord::getOrderTime, start);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX);
            wrapper.le(OrderRecord::getOrderTime, end);
        }
        wrapper.orderByDesc(OrderRecord::getCreateTime);

        Page<OrderRecord> pageResult = orderRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public OrderRecord getOrderById(Long id) {
        OrderRecord order = orderRecordMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        return order;
    }

    @Override
    public OrderRecord createOrder(OrderCreateRequest request) {
        Recipe recipe = recipeMapper.selectById(request.getRecipeId());
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }

        OrderRecord order = new OrderRecord();
        order.setCoupleId(request.getCoupleId());
        order.setRecipeId(request.getRecipeId());
        order.setRecipeName(recipe.getName());
        order.setRecipeImage(recipe.getCoverImage());
        order.setStatus(0);
        order.setRemark(request.getRemark());
        order.setOrderTime(LocalDateTime.now());
        order.setSyncId(UUID.randomUUID().toString());
        order.setSyncTime(LocalDateTime.now());

        // 记录下单用户（食客）
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
                order.setUserId(userDetails.getUserId());
            }
        } catch (Exception ignored) {
        }

        orderRecordMapper.insert(order);

        return order;
    }

    @Override
    public OrderRecord acceptOrder(Long id) {
        OrderRecord order = getOrderById(id);
        if (order.getStatus() != 0) {
            throw new BusinessException("Order cannot be accepted in current status");
        }
        order.setStatus(1);
        order.setAcceptTime(LocalDateTime.now());
        order.setSyncTime(LocalDateTime.now());
        orderRecordMapper.updateById(order);
        return order;
    }

    @Override
    public OrderRecord cookingOrder(Long id) {
        OrderRecord order = getOrderById(id);
        if (order.getStatus() != 1) {
            throw new BusinessException("Order cannot be set to cooking in current status");
        }
        order.setStatus(2);
        order.setSyncTime(LocalDateTime.now());
        orderRecordMapper.updateById(order);
        return order;
    }

    @Override
    public OrderRecord completeOrder(Long id) {
        OrderRecord order = getOrderById(id);
        if (order.getStatus() != 2) {
            throw new BusinessException("Order cannot be completed in current status");
        }
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        order.setSyncTime(LocalDateTime.now());
        orderRecordMapper.updateById(order);

        Recipe recipe = recipeMapper.selectById(order.getRecipeId());
        if (recipe != null) {
            recipe.setOrderCount(recipe.getOrderCount() == null ? 1 : recipe.getOrderCount() + 1);
            recipeMapper.updateById(recipe);
        }

        return order;
    }

    @Override
    public OrderRecord cancelOrder(Long id, String reason) {
        OrderRecord order = getOrderById(id);
        if (order.getStatus() == 3 || order.getStatus() == 4) {
            throw new BusinessException("Order cannot be cancelled in current status");
        }
        order.setStatus(4);
        order.setRejectReason(reason);
        order.setSyncTime(LocalDateTime.now());
        orderRecordMapper.updateById(order);
        return order;
    }

    @Override
    public void deleteOrder(Long id) {
        OrderRecord order = orderRecordMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        orderRecordMapper.deleteById(id);
    }

    @Override
    public void batchDeleteOrders(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        orderRecordMapper.deleteBatchIds(ids);
    }

    @Override
    public StatisticsResponse getStats(Long coupleId, String startDate, String endDate) {
        LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(coupleId != null, OrderRecord::getCoupleId, coupleId);
        if (StringUtils.hasText(startDate)) {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            wrapper.ge(OrderRecord::getOrderTime, start);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX);
            wrapper.le(OrderRecord::getOrderTime, end);
        }
        List<OrderRecord> orders = orderRecordMapper.selectList(wrapper);

        long total = orders.size();
        long pending = orders.stream().filter(o -> o.getStatus() == 0).count();
        long accepted = orders.stream().filter(o -> o.getStatus() == 1).count();
        long cooking = orders.stream().filter(o -> o.getStatus() == 2).count();
        long completed = orders.stream().filter(o -> o.getStatus() == 3).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == 4).count();

        StatisticsResponse response = new StatisticsResponse();
        Map<String, Long> categoryStats = new LinkedHashMap<>();
        categoryStats.put("pending", pending);
        categoryStats.put("accepted", accepted);
        categoryStats.put("cooking", cooking);
        categoryStats.put("completed", completed);
        categoryStats.put("cancelled", cancelled);
        response.setCategoryStats(categoryStats);
        return response;
    }
}
