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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "认证管理", description = "登录、注册、用户信息、密码管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PermissionService permissionService;

    @OperationLog(action = "LOGIN", target = "用户认证")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @OperationLog(action = "CREATE", target = "用户认证")
    @Operation(summary = "用户注册", description = "注册新账号，可选择主厨或食客角色")
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @OperationLog(action = "QUERY", target = "用户认证")
    @Operation(summary = "获取当前用户信息", description = "需要 JWT Token 认证")
    @GetMapping("/info")
    public ApiResponse<User> getCurrentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @OperationLog(action = "UPDATE", target = "用户认证")
    @Operation(summary = "修改密码", description = "需要提供旧密码和新密码")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        authService.changePassword(oldPassword, newPassword);
        return ApiResponse.success();
    }

    @OperationLog(action = "RESET", target = "用户认证")
    @Operation(summary = "重置密码", description = "通过用户名重置密码为新密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getNewPassword());
        return ApiResponse.success();
    }

    @Operation(summary = "发送重置密码验证码", description = "向用户绑定的邮箱发送6位验证码")
    @PostMapping("/send-reset-code")
    public ApiResponse<Void> sendResetCode(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            throw new com.shiyu.common.BusinessException("请输入用户名");
        }
        authService.sendResetCode(username.trim());
        return ApiResponse.success();
    }

    @Operation(summary = "验证码重置密码", description = "通过验证码重置密码")
    @PostMapping("/verify-reset-password")
    public ApiResponse<Void> verifyResetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        if (username == null || code == null || newPassword == null) {
            throw new com.shiyu.common.BusinessException("参数不完整");
        }
        authService.verifyAndResetPassword(username.trim(), code.trim(), newPassword);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "用户认证")
    @Operation(summary = "更新个人资料", description = "修改昵称和头像")
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(authService.updateProfile(request.getNickname(), request.getAvatar()));
    }

    @OperationLog(action = "QUERY", target = "用户认证")
    @Operation(summary = "获取用户权限列表", description = "返回当前用户的权限树")
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(permissionService.getUserPermissions(userDetails.getUserId()));
    }
}
