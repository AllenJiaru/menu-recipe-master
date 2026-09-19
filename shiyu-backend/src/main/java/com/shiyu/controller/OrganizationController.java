package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.entity.Organization;
import com.shiyu.entity.OrganizationInvite;
import com.shiyu.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public ApiResponse<List<Organization>> getUserOrganizations(Authentication auth) {
        Long userId = getUserId(auth);
        return ApiResponse.success(organizationService.getUserOrganizations(userId));
    }

    // ===== 机构管理 =====

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getOrganizationTree() {
        return ApiResponse.success(organizationService.getOrganizationTree());
    }

    @GetMapping("/by-type")
    public ApiResponse<List<Organization>> getOrganizationsByType(@RequestParam String type) {
        return ApiResponse.success(organizationService.getOrganizationsByType(type));
    }

    @GetMapping("/search")
    public ApiResponse<List<Organization>> searchOrganizations(@RequestParam String keyword) {
        return ApiResponse.success(organizationService.searchOrganizations(keyword));
    }

    @GetMapping("/{id}/children")
    public ApiResponse<List<Map<String, Object>>> getOrganizationChildren(@PathVariable Long id) {
        return ApiResponse.success(organizationService.getOrganizationChildren(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Organization> getOrganization(@PathVariable Long id) {
        Organization org = organizationService.getOrganization(id);
        if (org == null) return ApiResponse.error("组织不存在");
        return ApiResponse.success(org);
    }

    @PostMapping
    public ApiResponse<Organization> createOrganization(@RequestBody Organization org, Authentication auth) {
        Long userId = getUserId(auth);
        return ApiResponse.success(organizationService.createOrganization(org, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Organization> updateOrganization(
            @PathVariable Long id,
            @RequestBody Organization org,
            Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isAdmin(id, userId)) {
            return ApiResponse.error("无权限修改该组织");
        }
        return ApiResponse.success(organizationService.updateOrganization(id, org));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrganization(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isOwner(id, userId)) {
            return ApiResponse.error("只有组织所有者可以删除组织");
        }
        organizationService.deleteOrganization(id);
        return ApiResponse.success(null);
    }

    // ===== 成员管理 =====

    @GetMapping("/{id}/members")
    public ApiResponse<List<Map<String, Object>>> getMembers(@PathVariable Long id) {
        return ApiResponse.success(organizationService.getMembersWithDetails(id));
    }

    @PostMapping("/{id}/members")
    public ApiResponse<Void> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isAdmin(id, userId)) {
            return ApiResponse.error("无权限添加成员");
        }
        Long targetUserId = Long.valueOf(body.get("userId").toString());
        String role = (String) body.getOrDefault("role", "member");
        String nickname = (String) body.get("nickname");
        organizationService.addMember(id, targetUserId, role, nickname);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ApiResponse<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication auth) {
        Long currentUserId = getUserId(auth);
        if (!organizationService.isAdmin(id, currentUserId)) {
            return ApiResponse.error("无权限移除成员");
        }
        organizationService.removeMember(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long currentUserId = getUserId(auth);
        if (!organizationService.isOwner(id, currentUserId)) {
            return ApiResponse.error("只有组织所有者可以修改成员角色");
        }
        organizationService.updateMemberRole(id, userId, body.get("role"));
        return ApiResponse.success(null);
    }

    // ===== 用户搜索 =====

    @GetMapping("/{id}/search-users")
    public ApiResponse<List<Map<String, Object>>> searchUsers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String keyword,
            Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isAdmin(id, userId)) {
            return ApiResponse.error("无权限搜索用户");
        }
        return ApiResponse.success(organizationService.searchUsersNotInOrg(id, keyword));
    }

    // ===== 邀请管理 =====

    @GetMapping("/{id}/invites")
    public ApiResponse<List<OrganizationInvite>> getInvites(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isAdmin(id, userId)) {
            return ApiResponse.error("无权限查看邀请记录");
        }
        return ApiResponse.success(organizationService.getInvites(id));
    }

    @PostMapping("/{id}/invite")
    public ApiResponse<OrganizationInvite> createInvite(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isAdmin(id, userId)) {
            return ApiResponse.error("无权限创建邀请");
        }
        String role = body.getOrDefault("role", "member");
        String inviteeUsername = body.get("inviteeUsername");
        return ApiResponse.success(organizationService.createInvite(id, userId, role, inviteeUsername));
    }

    @PostMapping("/join")
    public ApiResponse<Map<String, Object>> joinByInviteCode(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = getUserId(auth);
        String code = body.get("inviteCode");
        if (code == null || code.isBlank()) {
            return ApiResponse.error("邀请码不能为空");
        }
        try {
            return ApiResponse.success(organizationService.joinByInviteCode(code.toUpperCase(), userId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ===== 统计 =====

    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> getOrgStats(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (!organizationService.isMember(id, userId)) {
            return ApiResponse.error("您不是该组织成员");
        }
        return ApiResponse.success(organizationService.getOrgStats(id));
    }

    // ===== 工具方法 =====

    @GetMapping("/{id}/check-member")
    public ApiResponse<Boolean> checkMember(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        return ApiResponse.success(organizationService.isMember(id, userId));
    }

    @GetMapping("/current")
    public ApiResponse<Long> getCurrentOrgId(Authentication auth) {
        Long userId = getUserId(auth);
        Long orgId = organizationService.getCurrentOrgId(userId);
        return ApiResponse.success(orgId);
    }

    private Long getUserId(Authentication auth) {
        if (auth.getPrincipal() instanceof com.shiyu.security.UserDetailsImpl) {
            return ((com.shiyu.security.UserDetailsImpl) auth.getPrincipal()).getUser().getId();
        }
        throw new RuntimeException("未登录或登录已过期");
    }
}
