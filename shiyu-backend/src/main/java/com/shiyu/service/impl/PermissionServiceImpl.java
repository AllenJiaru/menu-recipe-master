package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.PermissionRequest;
import com.shiyu.entity.SysPermission;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.SysRolePermission;
import com.shiyu.entity.User;
import com.shiyu.mapper.SysPermissionMapper;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysRolePermissionMapper;
import com.shiyu.mapper.UserMapper;
import com.shiyu.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getPermissionTree() {
        List<SysPermission> allPermissions = sysPermissionMapper.findTree();
        return buildTree(allPermissions);
    }

    @Override
    public List<Map<String, Object>> getUserPermissions(Long userId) {
        return buildTree(loadUserPermissions(userId));
    }

    private List<SysPermission> loadUserPermissions(Long userId) {
        List<SysPermission> userPermissions = sysPermissionMapper.findByUserId(userId);
        if (userPermissions.isEmpty()) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getRole() != null && !user.getRole().isBlank()) {
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRole::getCode, user.getRole());
                SysRole role = sysRoleMapper.selectOne(wrapper);
                if (role != null) {
                    userPermissions = sysPermissionMapper.findByRoleId(role.getId());
                }
            }
        }
        return userPermissions;
    }

    @Override
    public void createPermission(PermissionRequest request) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getCode, request.getCode());
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Permission code already exists");
        }

        SysPermission permission = new SysPermission();
        permission.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        permission.setName(request.getName());
        permission.setCode(request.getCode());
        permission.setType(request.getType() != null ? request.getType() : 0);
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setIcon(request.getIcon());
        permission.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        permission.setVisible(request.getVisible() != null ? request.getVisible() : 1);
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        permission.setDeleted(0);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.insert(permission);
    }

    @Override
    public void updatePermission(Long id, PermissionRequest request) {
        SysPermission existing = sysPermissionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Permission not found");
        }

        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getCode, request.getCode());
        wrapper.ne(SysPermission::getId, id);
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Permission code already exists");
        }

        existing.setParentId(request.getParentId() != null ? request.getParentId() : existing.getParentId());
        existing.setName(request.getName());
        existing.setCode(request.getCode());
        if (request.getType() != null) {
            existing.setType(request.getType());
        }
        existing.setPath(request.getPath());
        existing.setComponent(request.getComponent());
        existing.setIcon(request.getIcon());
        if (request.getSortOrder() != null) {
            existing.setSortOrder(request.getSortOrder());
        }
        if (request.getVisible() != null) {
            existing.setVisible(request.getVisible());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.updateById(existing);
    }

    @Override
    public void deletePermission(Long id) {
        SysPermission existing = sysPermissionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Permission not found");
        }

        LambdaQueryWrapper<SysPermission> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysPermission::getParentId, id);
        if (sysPermissionMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("Cannot delete permission that has children");
        }

        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getPermissionId, id);
        if (sysRolePermissionMapper.selectCount(rpWrapper) > 0) {
            throw new BusinessException("Cannot delete permission that is assigned to roles");
        }

        sysPermissionMapper.deleteById(id);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<SysPermission> userPermissions = loadUserPermissions(userId);
        return userPermissions.stream().anyMatch(p -> permissionCode.equals(p.getCode()));
    }

    private List<Map<String, Object>> buildTree(List<SysPermission> permissions) {
        Map<Long, SysPermission> permissionMap = new LinkedHashMap<>();
        for (SysPermission p : permissions) {
            permissionMap.put(p.getId(), p);
        }

        List<Map<String, Object>> tree = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> childrenMap = new HashMap<>();

        for (SysPermission p : permissions) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", p.getId());
            node.put("parentId", p.getParentId());
            node.put("name", p.getName());
            node.put("code", p.getCode());
            node.put("type", p.getType());
            node.put("path", p.getPath());
            node.put("component", p.getComponent());
            node.put("icon", p.getIcon());
            node.put("sortOrder", p.getSortOrder());
            node.put("visible", p.getVisible());
            node.put("status", p.getStatus());

            boolean parentMissing = p.getParentId() == null || p.getParentId() == 0L
                    || !permissionMap.containsKey(p.getParentId());
            if (parentMissing) {
                tree.add(node);
            } else {
                childrenMap.computeIfAbsent(p.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        addChildren(tree, childrenMap);
        return tree;
    }

    private void addChildren(List<Map<String, Object>> nodes, Map<Long, List<Map<String, Object>>> childrenMap) {
        for (Map<String, Object> node : nodes) {
            Long id = ((Number) node.get("id")).longValue();
            List<Map<String, Object>> children = childrenMap.get(id);
            if (children != null && !children.isEmpty()) {
                node.put("children", children);
                addChildren(children, childrenMap);
            } else {
                node.put("children", new ArrayList<>());
            }
        }
    }
}
