package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.SyncRequest;
import com.shiyu.service.SyncService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/pull")
    public ApiResponse<Map<String, Object>> pull(@Valid @RequestBody SyncRequest request) {
        return ApiResponse.success(syncService.pull(request));
    }

    @PostMapping("/push")
    public ApiResponse<Map<String, Object>> push(@Valid @RequestBody SyncRequest request) {
        return ApiResponse.success(syncService.push(request));
    }

    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> getSyncLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.success(syncService.getSyncLogs(page, size));
    }
}
