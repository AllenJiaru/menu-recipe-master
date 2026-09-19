package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    @GetMapping
    public ApiResponse<Map<String, Object>> getSystemHealth() {
        return ApiResponse.success(healthService.getSystemHealth());
    }

    @GetMapping("/database")
    public ApiResponse<Map<String, Object>> getDatabaseHealth() {
        return ApiResponse.success(healthService.getDatabaseHealth());
    }

    @GetMapping("/disk")
    public ApiResponse<Map<String, Object>> getDiskHealth() {
        return ApiResponse.success(healthService.getDiskHealth());
    }

    @GetMapping("/cache")
    public ApiResponse<Map<String, Object>> getCacheHealth() {
        return ApiResponse.success(healthService.getCacheHealth());
    }

    @PostMapping("/cache/clear")
    public ApiResponse<Void> clearCache() {
        healthService.clearCache();
        return ApiResponse.success();
    }

    @PostMapping("/database/optimize")
    public ApiResponse<Void> optimizeDatabase() {
        healthService.optimizeDatabase();
        return ApiResponse.success();
    }

    @GetMapping("/backups")
    public ApiResponse<Map<String, Object>> getBackupList() {
        return ApiResponse.success(healthService.getBackupList());
    }

    @PostMapping("/backups/create")
    public ApiResponse<Void> createBackup() {
        healthService.createBackup();
        return ApiResponse.success();
    }

    @PostMapping("/backups/{name}/restore")
    public ApiResponse<Void> restoreBackup(@PathVariable String name) {
        healthService.restoreBackup(name);
        return ApiResponse.success();
    }
}
