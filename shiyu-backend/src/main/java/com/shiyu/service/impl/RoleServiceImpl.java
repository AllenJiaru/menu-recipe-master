package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.RoleRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.SysPermission;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.SysRolePermission;
import com.shiyu.entity.SysUserRole;
import com.shiyu.mapper.SysPermissionMapper;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysRolePermissionMapper;
import com.shiyu.mapper.SysUserRoleMapper;
import com.shiyu.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public PageResponse<SysRole> getRoles(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), SysRole::getName, keyword)
                .or()
                .like(StringUtils.hasText(keyword), SysRole::getCode, keyword);
        wrapper.eq(status != null, SysRole::getStatus, status);
        wrapper.orderByAsc(SysRole::getSortOrder);

        Page<SysRole> pageResult = sysRoleMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public SysRole getRoleById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        return role;
    }

    @Override
    @Transactional
    public void createRole(RoleRequest request) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, request.getCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Role code already exists");
        }

        SysRole role = new SysRole();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        role.setDeleted(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            for (Long permissionId : request.getPermissionIds()) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permissionId);
                rp.setCreateTime(LocalDateTime.now());
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleRequest request) {
        SysRole existing = sysRoleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Role not found");
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, request.getCode());
        wrapper.ne(SysRole::getId, id);
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Role code already exists");
        }

        existing.setName(request.getName());
        existing.setCode(request.getCode());
        existing.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            existing.setSortOrder(request.getSortOrder());
        }
        existing.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(existing);

        if (request.getPermissionIds() != null) {
            sysRolePermissionMapper.deleteByRoleId(id);
            for (Long permissionId : request.getPermissionIds()) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permissionId);
                rp.setCreateTime(LocalDateTime.now());
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole existing = sysRoleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Role not found");
        }

        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, id);
        if (sysUserRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Cannot delete role that is assigned to users");
        }

        // 先删除角色权限关联
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, id));
        
        sysRoleMapper.deleteById(id);
    }

    @Override
    public List<SysPermission> getRolePermissions(Long roleId) {
        return sysPermissionMapper.findByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw new BusinessException("Role not found");
        }

        sysRolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateTime(LocalDateTime.now());
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<SysRole> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSortOrder);
        return sysRoleMapper.selectList(wrapper);
    }
}
