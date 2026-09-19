package com.shiyu.service;

import com.shiyu.dto.request.OrderCreateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.dto.response.StatisticsResponse;
import com.shiyu.entity.OrderRecord;

import java.util.List;

public interface OrderService {
    PageResponse<OrderRecord> getOrders(Integer page, Integer size, Integer status, Long coupleId, String startDate, String endDate);
    OrderRecord getOrderById(Long id);
    OrderRecord createOrder(OrderCreateRequest request);
    OrderRecord acceptOrder(Long id);
    OrderRecord cookingOrder(Long id);
    OrderRecord completeOrder(Long id);
    OrderRecord cancelOrder(Long id, String reason);
    void deleteOrder(Long id);
    void batchDeleteOrders(List<Long> ids);
    StatisticsResponse getStats(Long coupleId, String startDate, String endDate);
}
