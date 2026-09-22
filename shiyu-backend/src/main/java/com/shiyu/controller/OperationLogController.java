package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.OperationLog;
import com.shiyu.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "操作日志", description = "操作审计日志查询")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "查询操作日志", description = "分页查询操作审计日志，支持关键词搜索")
    @GetMapping
    public ApiResponse<PageResponse<OperationLog>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(operationLogService.getLogs(page, size, keyword));
    }
}
