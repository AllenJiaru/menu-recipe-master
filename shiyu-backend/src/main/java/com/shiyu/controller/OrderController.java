package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.OrderCreateRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.dto.response.StatisticsResponse;
import com.shiyu.entity.OrderRecord;
import com.shiyu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "点餐、接单、制作、上菜全流程")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @OperationLog(action = "QUERY", target = "订单")
    @Operation(summary = "查询订单列表", description = "分页查询订单，支持状态、情侣、日期范围筛选")
    @GetMapping
    public ApiResponse<PageResponse<OrderRecord>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long coupleId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(orderService.getOrders(page, size, status, coupleId, startDate, endDate));
    }

    @OperationLog(action = "QUERY", target = "订单")
    @Operation(summary = "查询订单详情", description = "根据ID查询订单详细信息")
    @GetMapping("/{id}")
    public ApiResponse<OrderRecord> getOrderById(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderById(id));
    }

    @OperationLog(action = "CREATE", target = "订单")
    @Operation(summary = "创建订单", description = "提交新订单")
    @PostMapping
    public ApiResponse<OrderRecord> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @OperationLog(action = "ACCEPT", target = "订单")
    @Operation(summary = "接单", description = "接受订单并进入制作流程")
    @PutMapping("/{id}/accept")
    public ApiResponse<Void> acceptOrder(@PathVariable Long id) {
        orderService.acceptOrder(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "订单")
    @Operation(summary = "开始制作", description = "订单进入制作状态")
    @PutMapping("/{id}/cooking")
    public ApiResponse<Void> cookingOrder(@PathVariable Long id) {
        orderService.cookingOrder(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "COMPLETE", target = "订单")
    @Operation(summary = "完成订单", description = "标记订单为已完成")
    @PutMapping("/{id}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "CANCEL", target = "订单")
    @Operation(summary = "取消订单", description = "取消订单并填写取消原因")
    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.cancelOrder(id, body.get("reason"));
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "订单")
    @Operation(summary = "删除订单", description = "根据ID删除订单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "订单")
    @Operation(summary = "批量删除订单", description = "根据ID列表批量删除订单")
    @PostMapping("/batch/delete")
    public ApiResponse<Void> batchDeleteOrders(@RequestBody com.shiyu.dto.request.BatchRequest request) {
        orderService.batchDeleteOrders(request.getIds());
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "订单统计")
    @Operation(summary = "查询订单统计", description = "获取订单统计数据，支持日期范围筛选")
    @GetMapping("/stats")
    public ApiResponse<StatisticsResponse> getStats(
            @RequestParam(required = false) Long coupleId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(orderService.getStats(coupleId, startDate, endDate));
    }
}
