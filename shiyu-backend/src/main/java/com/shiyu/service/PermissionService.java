package com.shiyu.service;

import com.shiyu.dto.request.PermissionRequest;

import java.util.List;
import java.util.Map;

public interface PermissionService {
    List<Map<String, Object>> getPermissionTree();
    List<Map<String, Object>> getUserPermissions(Long userId);
    void createPermission(PermissionRequest request);
    void updatePermission(Long id, PermissionRequest request);
    void deletePermission(Long id);
    boolean hasPermission(Long userId, String permissionCode);
}
