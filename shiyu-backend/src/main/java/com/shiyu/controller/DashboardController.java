package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.DashboardResponse;
import com.shiyu.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "仪表板", description = "数据统计概览")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "查询数据概览", description = "获取仪表板统计数据概览")
    @GetMapping("/overview")
    public ApiResponse<DashboardResponse> getOverview() {
        return ApiResponse.success(dashboardService.getOverview());
    }
}
