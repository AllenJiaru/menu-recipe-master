package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.StatisticsResponse;
import com.shiyu.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/orders")
    public ApiResponse<StatisticsResponse> getOrderStats(
            @RequestParam(required = false) Long coupleId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(statisticsService.getOrderStats(coupleId, startDate, endDate));
    }
}
