package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.SysUserRole;
import com.shiyu.entity.User;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysUserRoleMapper;
import com.shiyu.mapper.UserMapper;
import com.shiyu.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public PageResponse<Map<String, Object>> getUsersWithRoles(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or()
                .like(StringUtils.hasText(keyword), User::getNickname, keyword);
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);

        List<Map<String, Object>> userList = new ArrayList<>();
        for (User user : pageResult.getRecords()) {
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatar());
            userMap.put("status", user.getStatus());
            userMap.put("createTime", user.getCreateTime());

            LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(SysUserRole::getUserId, user.getId());
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(urWrapper);

            List<String> roleNames = new ArrayList<>();
            for (SysUserRole ur : userRoles) {
                SysRole role = sysRoleMapper.selectById(ur.getRoleId());
                if (role != null) {
                    roleNames.add(role.getName());
                }
            }
            userMap.put("roles", roleNames);

            userList.add(userMap);
        }

        return PageResponse.of(userList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        sysUserRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysRole role = sysRoleMapper.selectById(roleId);
                if (role == null) {
                    throw new BusinessException("Role not found: " + roleId);
                }
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setCreateTime(LocalDateTime.now());
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.findByUserId(userId);
        List<SysRole> roles = new ArrayList<>();
        for (SysUserRole ur : userRoles) {
            SysRole role = sysRoleMapper.selectById(ur.getRoleId());
            if (role != null) {
                roles.add(role);
            }
        }
        return roles;
    }
}
