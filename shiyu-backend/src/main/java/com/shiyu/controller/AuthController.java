package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import com.shiyu.dto.request.LoginRequest;
import com.shiyu.dto.request.RegisterRequest;
import com.shiyu.dto.request.ResetPasswordRequest;
import com.shiyu.dto.request.UpdateProfileRequest;
import com.shiyu.dto.response.LoginResponse;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.AuthService;
import com.shiyu.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PermissionService permissionService;

    @OperationLog(action = "LOGIN", target = "用户认证")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @OperationLog(action = "CREATE", target = "用户认证")
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @OperationLog(action = "QUERY", target = "用户认证")
    @GetMapping("/info")
    public ApiResponse<User> getCurrentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @OperationLog(action = "UPDATE", target = "用户认证")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        authService.changePassword(oldPassword, newPassword);
        return ApiResponse.success();
    }

    @OperationLog(action = "RESET", target = "用户认证")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getNewPassword());
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户认证")
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(authService.updateProfile(request.getNickname(), request.getAvatar()));
    }

    @OperationLog(action = "QUERY", target = "用户认证")
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.getUserPermissions(userDetails.getUserId()));
    }
}
