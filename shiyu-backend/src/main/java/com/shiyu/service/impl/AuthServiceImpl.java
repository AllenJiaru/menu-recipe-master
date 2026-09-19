package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.LoginRequest;
import com.shiyu.dto.request.RegisterRequest;
import com.shiyu.dto.response.LoginResponse;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.SysUserRole;
import com.shiyu.entity.User;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysUserRoleMapper;
import com.shiyu.mapper.UserMapper;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.AuthService;
import com.shiyu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("Username or password incorrect");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Username or password incorrect");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("Account has been disabled");
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest httpRequest = attrs.getRequest();
                String ip = httpRequest.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) ip = httpRequest.getRemoteAddr();
                user.setLastLoginIp(ip);
            }
        } catch (Exception ignored) {}
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(request.getRole() != null ? request.getRole() : "user");
        user.setStatus(1);
        user.setLoginCount(0);
        user.setDeleted(0);
        userMapper.insert(user);

        // 建立用户-角色关联，供权限查询使用
        bindUserRole(user.getId(), user.getRole());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectById(userDetails.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectById(userDetails.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(String username, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public User updateProfile(String nickname, String avatar) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectById(userDetails.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    private void bindUserRole(Long userId, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return;
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, roleCode);
        SysRole role = sysRoleMapper.selectOne(wrapper);
        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }
    }
}
