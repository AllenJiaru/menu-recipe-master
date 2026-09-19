package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.OrganizationMember;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.SysUserRole;
import com.shiyu.entity.User;
import com.shiyu.mapper.OrganizationMemberMapper;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysUserRoleMapper;
import com.shiyu.mapper.UserMapper;
import com.shiyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private OrganizationMemberMapper organizationMemberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<User> getUsers(Integer page, Integer size, String keyword, String role, Long organizationId) {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or()
                .like(StringUtils.hasText(keyword), User::getNickname, keyword);
        wrapper.eq(StringUtils.hasText(role), User::getRole, role);
        wrapper.eq(organizationId != null, User::getOrganizationId, organizationId);
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insert(user);
        syncUserRole(user.getId(), user.getRole());
        syncOrganizationMember(user.getId(), user.getOrganizationId(), user.getRole());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("User not found");
        }
        // 密码为空时不更新密码
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setId(id);
        userMapper.updateById(user);
        // 同步组织成员关系
        syncOrganizationMember(id, user.getOrganizationId(), user.getRole());
        return userMapper.selectById(id);
    }

    @Override
    public void deleteUser(Long id) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("User not found");
        }
        userMapper.deleteById(id);
        // 清理组织成员关系
        organizationMemberMapper.delete(new LambdaQueryWrapper<OrganizationMember>()
                .eq(OrganizationMember::getUserId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("User not found");
        }
        existing.setStatus(status);
        userMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void updateRole(Long id, String role) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("User not found");
        }
        existing.setRole(role);
        userMapper.updateById(existing);
        syncUserRole(id, role);
        // 同步组织成员角色
        if (existing.getOrganizationId() != null) {
            syncOrganizationMember(id, existing.getOrganizationId(), role);
        }
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("User not found");
        }
        String newPassword = UUID.randomUUID().toString().substring(0, 6);
        existing.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(existing);
        return newPassword;
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setStatus(status);
                userMapper.updateById(user);
            }
        }
    }

    @Override
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            userMapper.deleteById(id);
            organizationMemberMapper.delete(new LambdaQueryWrapper<OrganizationMember>()
                    .eq(OrganizationMember::getUserId, id));
        }
    }

    private void syncUserRole(Long userId, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return;
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, roleCode);
        SysRole role = sysRoleMapper.selectOne(wrapper);
        if (role == null) {
            return;
        }
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        SysUserRole existing = sysUserRoleMapper.selectOne(urWrapper);
        if (existing == null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        } else if (!existing.getRoleId().equals(role.getId())) {
            existing.setRoleId(role.getId());
            sysUserRoleMapper.updateById(existing);
        }
    }

    private void syncOrganizationMember(Long userId, Long organizationId, String role) {
        if (organizationId == null) {
            // 如果组织ID为空，删除该用户的组织成员关系
            organizationMemberMapper.delete(new LambdaQueryWrapper<OrganizationMember>()
                    .eq(OrganizationMember::getUserId, userId));
            return;
        }

        // 检查是否已存在成员关系
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId)
                .eq(OrganizationMember::getUserId, userId);
        OrganizationMember existing = organizationMemberMapper.selectOne(wrapper);

        // 将系统角色映射为组织角色
        String orgRole = mapToOrgRole(role);

        if (existing == null) {
            // 创建新的成员关系
            OrganizationMember member = new OrganizationMember();
            member.setOrganizationId(organizationId);
            member.setUserId(userId);
            member.setRole(orgRole);
            member.setStatus(1);
            member.setJoinTime(LocalDateTime.now());
            organizationMemberMapper.insert(member);
        } else {
            // 更新现有成员关系的角色
            existing.setRole(orgRole);
            organizationMemberMapper.updateById(existing);
        }

        // 更新用户的默认组织
        User user = userMapper.selectById(userId);
        if (user != null && (user.getOrganizationId() == null || !user.getOrganizationId().equals(organizationId))) {
            user.setOrganizationId(organizationId);
            userMapper.updateById(user);
        }
    }

    private String mapToOrgRole(String systemRole) {
        if (systemRole == null) return "member";
        return switch (systemRole) {
            case "super_admin", "admin" -> "admin";
            default -> "member";
        };
    }
}
