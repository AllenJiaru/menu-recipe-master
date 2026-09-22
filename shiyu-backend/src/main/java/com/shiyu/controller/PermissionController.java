package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.PermissionRequest;
import com.shiyu.entity.SysPermission;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "权限管理", description = "权限树、菜单管理")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @OperationLog(action = "QUERY", target = "权限")
    @Operation(summary = "查询权限树", description = "获取完整的权限树结构")
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getPermissionTree() {
        return ApiResponse.success(permissionService.getPermissionTree());
    }

    @OperationLog(action = "QUERY", target = "权限")
    @Operation(summary = "查询用户权限", description = "获取当前登录用户的权限列表")
    @GetMapping("/user")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.getUserPermissions(userDetails.getUserId()));
    }

    @OperationLog(action = "CREATE", target = "权限")
    @Operation(summary = "创建权限", description = "新增权限")
    @PostMapping
    public ApiResponse<Void> createPermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.createPermission(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "权限")
    @Operation(summary = "更新权限", description = "修改权限信息")
    @PutMapping("/{id}")
    public ApiResponse<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        permissionService.updatePermission(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "权限")
    @Operation(summary = "删除权限", description = "根据ID删除权限")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "权限")
    @Operation(summary = "检查权限", description = "检查当前用户是否拥有指定权限码")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkPermission(@RequestParam String code) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.hasPermission(userDetails.getUserId(), code));
    }
}
