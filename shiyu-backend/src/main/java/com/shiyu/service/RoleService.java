package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.dto.request.RoleRequest;
import com.shiyu.entity.SysPermission;
import com.shiyu.entity.SysRole;

import java.util.List;

public interface RoleService {
    PageResponse<SysRole> getRoles(int page, int size, String keyword, Integer status);
    SysRole getRoleById(Long id);
    void createRole(RoleRequest request);
    void updateRole(Long id, RoleRequest request);
    void deleteRole(Long id);
    List<SysPermission> getRolePermissions(Long roleId);
    void assignPermissions(Long roleId, List<Long> permissionIds);
    List<SysRole> getAllRoles();
}
