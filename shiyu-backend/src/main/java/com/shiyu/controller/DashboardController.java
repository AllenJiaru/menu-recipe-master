package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.DashboardResponse;
import com.shiyu.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public ApiResponse<DashboardResponse> getOverview() {
        return ApiResponse.success(dashboardService.getOverview());
    }
}
