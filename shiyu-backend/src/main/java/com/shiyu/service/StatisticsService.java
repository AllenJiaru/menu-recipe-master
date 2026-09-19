package com.shiyu.service;

import com.shiyu.dto.response.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse getOrderStats(Long coupleId, String startDate, String endDate);
}
