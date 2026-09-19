package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.SysRole;
import com.shiyu.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-management")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> getUsersWithRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(userManagementService.getUsersWithRoles(page, size, keyword));
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userManagementService.assignRoles(userId, roleIds);
        return ApiResponse.success();
    }

    @GetMapping("/{userId}/roles")
    public ApiResponse<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        return ApiResponse.success(userManagementService.getUserRoles(userId));
    }
}
