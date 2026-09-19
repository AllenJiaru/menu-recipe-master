package com.shiyu.service;

import com.shiyu.entity.Organization;
import com.shiyu.entity.OrganizationMember;
import com.shiyu.entity.OrganizationInvite;
import java.util.List;
import java.util.Map;

public interface OrganizationService {
    
    // 组织管理
    List<Organization> getUserOrganizations(Long userId);
    Organization getOrganization(Long orgId);
    Organization createOrganization(Organization org, Long userId);
    Organization updateOrganization(Long orgId, Organization org);
    void deleteOrganization(Long orgId);
    
    // 机构管理
    List<Map<String, Object>> getOrganizationTree();
    List<Organization> getOrganizationsByType(String type);
    List<Organization> searchOrganizations(String keyword);
    List<Map<String, Object>> getOrganizationChildren(Long parentId);
    
    // 成员管理
    List<Map<String, Object>> getMembersWithDetails(Long orgId);
    List<OrganizationMember> getMembers(Long orgId);
    OrganizationMember addMember(Long orgId, Long userId, String role, String nickname);
    void removeMember(Long orgId, Long userId);
    OrganizationMember updateMemberRole(Long orgId, Long userId, String role);
    
    // 用户搜索
    List<Map<String, Object>> searchUsersNotInOrg(Long orgId, String keyword);
    
    // 邀请管理
    List<OrganizationInvite> getInvites(Long orgId);
    OrganizationInvite createInvite(Long orgId, Long inviterId, String role, String inviteeUsername);
    Map<String, Object> joinByInviteCode(String inviteCode, Long userId);
    
    // 工具方法
    boolean isMember(Long orgId, Long userId);
    boolean isOwner(Long orgId, Long userId);
    boolean isAdmin(Long orgId, Long userId);
    Long getCurrentOrgId(Long userId);
    Map<String, Object> getOrgStats(Long orgId);
}
