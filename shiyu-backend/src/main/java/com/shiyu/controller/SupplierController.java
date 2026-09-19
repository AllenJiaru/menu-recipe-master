package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.SupplierRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Supplier;
import com.shiyu.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @OperationLog(action = "QUERY", target = "供应商")
    @GetMapping
    public ApiResponse<PageResponse<Supplier>> getSuppliers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(supplierService.getSuppliers(page, size, keyword, category));
    }

    @OperationLog(action = "QUERY", target = "供应商")
    @GetMapping("/{id}")
    public ApiResponse<Supplier> getSupplierById(@PathVariable Long id) {
        return ApiResponse.success(supplierService.getSupplierById(id));
    }

    @OperationLog(action = "CREATE", target = "供应商")
    @PostMapping
    public ApiResponse<Void> createSupplier(@Valid @RequestBody SupplierRequest request) {
        supplierService.createSupplier(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "供应商")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        supplierService.updateSupplier(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "供应商")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ApiResponse.success();
    }
}
