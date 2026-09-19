package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.User;
import com.shiyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @OperationLog(action = "QUERY", target = "用户")
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
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @OperationLog(action = "CREATE", target = "用户")
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user));
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ApiResponse.success(userService.updateUser(id, user));
    }

    @OperationLog(action = "DELETE", target = "用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.updateStatus(id, body.get("status"));
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.updateRole(id, body.get("role"));
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户")
    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return ApiResponse.success(newPassword);
    }

    @OperationLog(action = "BATCH_UPDATE", target = "用户")
    @PutMapping("/batch-status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) body.get("ids")).stream().map(Number::longValue).toList();
        Integer status = (Integer) body.get("status");
        userService.batchUpdateStatus(ids, status);
        return ApiResponse.success();
    }

    @OperationLog(action = "BATCH_DELETE", target = "用户")
    @DeleteMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        userService.batchDelete(body.get("ids"));
        return ApiResponse.success();
    }
}
