package com.shiyu.service;

import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.User;

import java.util.List;

public interface UserService {
    PageResponse<User> getUsers(Integer page, Integer size, String keyword, String role, Long organizationId);
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void updateStatus(Long id, Integer status);
    void updateRole(Long id, String role);
    String resetPassword(Long id);
    void batchUpdateStatus(List<Long> ids, Integer status);
    void batchDelete(List<Long> ids);
}
