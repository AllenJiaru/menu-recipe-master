package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.SysRole;

import java.util.List;
import java.util.Map;

public interface UserManagementService {
    PageResponse<Map<String, Object>> getUsersWithRoles(int page, int size, String keyword);
    void assignRoles(Long userId, List<Long> roleIds);
    List<SysRole> getUserRoles(Long userId);
}
