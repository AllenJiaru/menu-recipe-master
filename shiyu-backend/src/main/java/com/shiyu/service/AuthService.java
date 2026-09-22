package com.shiyu.service;

import com.shiyu.dto.request.LoginRequest;
import com.shiyu.dto.request.RegisterRequest;
import com.shiyu.dto.response.LoginResponse;
import com.shiyu.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    User getCurrentUser();
    void changePassword(String oldPassword, String newPassword);
    void resetPassword(String username, String newPassword);
    User updateProfile(String nickname, String avatar);
    void sendResetCode(String username);
    void verifyAndResetPassword(String username, String code, String newPassword);
}
