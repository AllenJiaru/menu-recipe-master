package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY sort_order")
    List<SysPermission> findTree();

    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND p.status = 1 AND r.deleted = 0 AND r.status = 1 " +
            "ORDER BY p.sort_order")
    List<SysPermission> findByRoleId(Long roleId);

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND p.status = 1 AND r.deleted = 0 AND r.status = 1 " +
            "ORDER BY p.sort_order")
    List<SysPermission> findByUserId(Long userId);

    @Select("SELECT code FROM sys_permission WHERE deleted = 0 AND status = 1")
    List<String> findAllActiveCodes();

    @Select("SELECT DISTINCT p.code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND p.status = 1 AND r.deleted = 0 AND r.status = 1")
    List<String> findActiveCodesByUserId(Long userId);
}
