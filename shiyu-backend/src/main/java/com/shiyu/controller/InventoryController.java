package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.InventoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Inventory;
import com.shiyu.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @OperationLog(action = "QUERY", target = "库存")
    @GetMapping
    public ApiResponse<PageResponse<Inventory>> getInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(inventoryService.getInventory(page, size, keyword, category));
    }

    @OperationLog(action = "QUERY", target = "库存")
    @GetMapping("/{id}")
    public ApiResponse<Inventory> getInventoryById(@PathVariable Long id) {
        return ApiResponse.success(inventoryService.getInventoryById(id));
    }

    @OperationLog(action = "CREATE", target = "库存")
    @PostMapping
    public ApiResponse<Void> createInventory(@Valid @RequestBody InventoryRequest request) {
        inventoryService.createInventory(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "库存")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        inventoryService.updateInventory(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "库存")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "库存")
    @GetMapping("/low-stock")
    public ApiResponse<List<Inventory>> getLowStockItems() {
        return ApiResponse.success(inventoryService.getLowStockItems());
    }

    @OperationLog(action = "UPDATE", target = "库存")
    @PutMapping("/{id}/restock")
    public ApiResponse<Void> restock(@PathVariable Long id, @RequestParam Double quantity) {
        inventoryService.restock(id, quantity);
        return ApiResponse.success();
    }
}
