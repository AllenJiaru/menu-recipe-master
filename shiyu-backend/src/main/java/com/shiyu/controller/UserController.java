package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.User;
import com.shiyu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户 CRUD、角色分配")
public class UserController {

    @Autowired
    private UserService userService;

    @OperationLog(action = "QUERY", target = "用户")
    @Operation(summary = "查询用户列表", description = "分页查询用户，支持关键词、角色、组织筛选")
    @GetMapping
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long organizationId) {
        return ApiResponse.success(userService.getUsers(page, size, keyword, role, organizationId));
    }

    @OperationLog(action = "QUERY", target = "用户")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @OperationLog(action = "CREATE", target = "用户")
    @Operation(summary = "创建用户", description = "新增用户")
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user));
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @Operation(summary = "更新用户", description = "修改用户信息")
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ApiResponse.success(userService.updateUser(id, user));
    }

    @OperationLog(action = "DELETE", target = "用户")
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.updateStatus(id, body.get("status"));
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @Operation(summary = "分配用户角色", description = "为用户指定角色")
    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.updateRole(id, body.get("role"));
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @Operation(summary = "重置密码", description = "重置用户密码并返回新密码")
    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return ApiResponse.success(newPassword);
    }

    @OperationLog(action = "BATCH_UPDATE", target = "用户")
    @Operation(summary = "批量更新用户状态", description = "根据ID列表批量启用或禁用用户")
    @PutMapping("/batch-status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) body.get("ids")).stream().map(Number::longValue).toList();
        Integer status = (Integer) body.get("status");
        userService.batchUpdateStatus(ids, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "BATCH_DELETE", target = "用户")
    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @DeleteMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        userService.batchDelete(body.get("ids"));
        return ApiResponse.success();
    }
}
