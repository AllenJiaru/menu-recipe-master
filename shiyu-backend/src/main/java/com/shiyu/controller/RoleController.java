package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.RoleRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.SysPermission;
import com.shiyu.entity.SysRole;
import com.shiyu.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @OperationLog(action = "QUERY", target = "角色")
    @GetMapping
    public ApiResponse<PageResponse<SysRole>> getRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(roleService.getRoles(page, size, keyword, status));
    }

    @OperationLog(action = "QUERY", target = "角色")
    @GetMapping("/all")
    public ApiResponse<List<SysRole>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @OperationLog(action = "QUERY", target = "角色")
    @GetMapping("/{id}")
    public ApiResponse<SysRole> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @OperationLog(action = "CREATE", target = "角色")
    @PostMapping
    public ApiResponse<Void> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "角色")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        roleService.updateRole(id, request);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "角色")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "角色")
    @GetMapping("/{id}/permissions")
    public ApiResponse<List<SysPermission>> getRolePermissions(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRolePermissions(id));
    }

    @OperationLog(action = "UPDATE", target = "角色")
    @PutMapping("/{id}/permissions")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ApiResponse.success();
    }
}
