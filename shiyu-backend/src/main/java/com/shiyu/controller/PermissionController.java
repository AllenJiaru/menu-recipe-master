package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.PermissionRequest;
import com.shiyu.entity.SysPermission;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @OperationLog(action = "QUERY", target = "权限")
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getPermissionTree() {
        return ApiResponse.success(permissionService.getPermissionTree());
    }

    @OperationLog(action = "QUERY", target = "权限")
    @GetMapping("/user")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.getUserPermissions(userDetails.getUserId()));
    }

    @OperationLog(action = "CREATE", target = "权限")
    @PostMapping
    public ApiResponse<Void> createPermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.createPermission(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "权限")
    @PutMapping("/{id}")
    public ApiResponse<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        permissionService.updatePermission(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "权限")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "权限")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkPermission(@RequestParam String code) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.hasPermission(userDetails.getUserId(), code));
    }
}
