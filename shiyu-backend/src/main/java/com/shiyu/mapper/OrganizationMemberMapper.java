package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.OrganizationMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrganizationMemberMapper extends BaseMapper<OrganizationMember> {
    
    @Select("SELECT om.id, om.organization_id AS organizationId, om.user_id AS userId, om.role, om.status, om.join_time AS joinTime, om.nickname_in_org AS nicknameInOrg, " +
            "u.username, u.nickname, u.avatar, u.email, u.phone " +
            "FROM organization_member om " +
            "LEFT JOIN sys_user u ON om.user_id = u.id " +
            "WHERE om.organization_id = #{orgId} AND om.status = 1")
    List<Map<String, Object>> findByOrgIdWithDetails(@Param("orgId") Long orgId);
    
    @Select("SELECT om.* FROM organization_member om " +
            "WHERE om.user_id = #{userId} AND om.status = 1")
    List<OrganizationMember> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT om.* FROM organization_member om " +
            "WHERE om.organization_id = #{orgId} AND om.user_id = #{userId} LIMIT 1")
    OrganizationMember findByOrgAndUser(@Param("orgId") Long orgId, @Param("userId") Long userId);
    
    @Delete("DELETE FROM organization_member WHERE organization_id = #{orgId} AND user_id = #{userId}")
    int deleteByOrgAndUser(@Param("orgId") Long orgId, @Param("userId") Long userId);
    
    @Select("SELECT u.id, u.username, u.nickname, u.avatar, u.email, u.phone " +
            "FROM sys_user u " +
            "WHERE u.deleted = 0 AND u.status = 1 " +
            "AND u.id NOT IN (" +
            "  SELECT om.user_id FROM organization_member om " +
            "  WHERE om.organization_id = #{orgId} AND om.status = 1" +
            ") " +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "LIMIT 20")
    List<Map<String, Object>> searchUsersNotInOrg(@Param("orgId") Long orgId, @Param("keyword") String keyword);
}
