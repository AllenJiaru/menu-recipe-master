package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.InventoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Inventory;
import com.shiyu.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "库存管理", description = "食材库存、低库存预警")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @OperationLog(action = "QUERY", target = "库存")
    @Operation(summary = "查询库存列表", description = "分页查询食材库存，支持关键词和分类筛选")
    @GetMapping
    public ApiResponse<PageResponse<Inventory>> getInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(inventoryService.getInventory(page, size, keyword, category));
    }

    @OperationLog(action = "QUERY", target = "库存")
    @Operation(summary = "查询库存详情", description = "根据ID查询库存详细信息")
    @GetMapping("/{id}")
    public ApiResponse<Inventory> getInventoryById(@PathVariable Long id) {
        return ApiResponse.success(inventoryService.getInventoryById(id));
    }

    @OperationLog(action = "CREATE", target = "库存")
    @Operation(summary = "创建库存", description = "新增食材库存记录")
    @PostMapping
    public ApiResponse<Void> createInventory(@Valid @RequestBody InventoryRequest request) {
        inventoryService.createInventory(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "库存")
    @Operation(summary = "更新库存", description = "修改库存信息")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        inventoryService.updateInventory(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "库存")
    @Operation(summary = "删除库存", description = "根据ID删除库存记录")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "库存")
    @Operation(summary = "查询低库存预警", description = "获取低于预警线的食材列表")
    @GetMapping("/low-stock")
    public ApiResponse<List<Inventory>> getLowStockItems() {
        return ApiResponse.success(inventoryService.getLowStockItems());
    }

    @OperationLog(action = "UPDATE", target = "库存")
    @Operation(summary = "补货", description = "为指定食材补货并更新库存数量")
    @PutMapping("/{id}/restock")
    public ApiResponse<Void> restock(@PathVariable Long id, @RequestParam Double quantity) {
        inventoryService.restock(id, quantity);
        return ApiResponse.success();
    }
}
