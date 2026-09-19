package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.entity.Organization;
import com.shiyu.entity.OrganizationMember;
import com.shiyu.entity.OrganizationInvite;
import com.shiyu.mapper.OrganizationMapper;
import com.shiyu.mapper.OrganizationMemberMapper;
import com.shiyu.mapper.OrganizationInviteMapper;
import com.shiyu.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private OrganizationMemberMapper memberMapper;

    @Autowired
    private OrganizationInviteMapper inviteMapper;

    @Override
    public List<Organization> getUserOrganizations(Long userId) {
        List<OrganizationMember> memberships = memberMapper.findByUserId(userId);
        if (memberships.isEmpty()) return Collections.emptyList();
        List<Long> orgIds = memberships.stream().map(OrganizationMember::getOrganizationId).toList();
        return organizationMapper.selectBatchIds(orgIds);
    }

    @Override
    public Organization getOrganization(Long orgId) {
        return organizationMapper.selectById(orgId);
    }

    @Override
    @Transactional
    public Organization createOrganization(Organization org, Long userId) {
        org.setOwnerId(userId);
        org.setStatus(1);
        org.setDeleted(0);
        
        // 设置层级和路径
        if (org.getParentId() != null && org.getParentId() > 0) {
            Organization parent = organizationMapper.selectById(org.getParentId());
            if (parent != null) {
                org.setLevel(parent.getLevel() + 1);
                org.setPath(parent.getPath() + "/" + parent.getId());
            }
        } else {
            org.setLevel(1);
            org.setPath("");
            org.setParentId(0L);
        }
        
        if (org.getSortOrder() == null) org.setSortOrder(0);
        if (org.getType() == null) org.setType("family");
        
        organizationMapper.insert(org);
        
        // 更新path包含自身ID
        org.setPath(org.getPath() + "/" + org.getId());
        organizationMapper.updateById(org);
        
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(org.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus(1);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);
        return org;
    }

    @Override
    public Organization updateOrganization(Long orgId, Organization org) {
        Organization existing = organizationMapper.selectById(orgId);
        if (existing == null) throw new RuntimeException("组织不存在");
        if (org.getName() != null) existing.setName(org.getName());
        if (org.getDescription() != null) existing.setDescription(org.getDescription());
        if (org.getAvatar() != null) existing.setAvatar(org.getAvatar());
        if (org.getType() != null) existing.setType(org.getType());
        if (org.getContactName() != null) existing.setContactName(org.getContactName());
        if (org.getContactPhone() != null) existing.setContactPhone(org.getContactPhone());
        if (org.getContactEmail() != null) existing.setContactEmail(org.getContactEmail());
        if (org.getAddress() != null) existing.setAddress(org.getAddress());
        if (org.getSortOrder() != null) existing.setSortOrder(org.getSortOrder());
        if (org.getSettings() != null) existing.setSettings(org.getSettings());
        if (org.getStatus() != null) existing.setStatus(org.getStatus());
        organizationMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteOrganization(Long orgId) {
        // 删除所有子机构
        List<Organization> children = organizationMapper.findByParentId(orgId);
        for (Organization child : children) {
            deleteOrganization(child.getId());
        }
        organizationMapper.deleteById(orgId);
        memberMapper.delete(new LambdaQueryWrapper<OrganizationMember>()
                .eq(OrganizationMember::getOrganizationId, orgId));
        inviteMapper.delete(new LambdaQueryWrapper<OrganizationInvite>()
                .eq(OrganizationInvite::getOrganizationId, orgId));
    }

    // ===== 机构管理 =====

    @Override
    public List<Map<String, Object>> getOrganizationTree() {
        List<Organization> all = organizationMapper.findAll();
        return buildTree(all, 0L);
    }

    @Override
    public List<Organization> getOrganizationsByType(String type) {
        return organizationMapper.findByType(type);
    }

    @Override
    public List<Organization> searchOrganizations(String keyword) {
        return organizationMapper.search(keyword);
    }

    @Override
    public List<Map<String, Object>> getOrganizationChildren(Long parentId) {
        List<Organization> children = organizationMapper.findByParentId(parentId);
        return children.stream().map(org -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", org.getId());
            map.put("name", org.getName());
            map.put("type", org.getType());
            map.put("description", org.getDescription());
            map.put("status", org.getStatus());
            map.put("level", org.getLevel());
            map.put("parentId", org.getParentId());
            map.put("contactName", org.getContactName());
            map.put("contactPhone", org.getContactPhone());
            map.put("address", org.getAddress());
            map.put("createTime", org.getCreateTime());
            return map;
        }).toList();
    }

    private List<Map<String, Object>> buildTree(List<Organization> all, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Organization org : all) {
            Long orgParentId = org.getParentId() != null ? org.getParentId() : 0L;
            if (orgParentId.equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", org.getId());
                node.put("name", org.getName());
                node.put("type", org.getType());
                node.put("description", org.getDescription());
                node.put("status", org.getStatus());
                node.put("level", org.getLevel());
                node.put("parentId", org.getParentId());
                node.put("contactName", org.getContactName());
                node.put("contactPhone", org.getContactPhone());
                node.put("address", org.getAddress());
                node.put("createTime", org.getCreateTime());
                node.put("children", buildTree(all, org.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    @Override
    public List<Map<String, Object>> getMembersWithDetails(Long orgId) {
        return memberMapper.findByOrgIdWithDetails(orgId);
    }

    @Override
    public List<OrganizationMember> getMembers(Long orgId) {
        return memberMapper.findByOrgIdWithDetails(orgId).stream().map(m -> {
            OrganizationMember member = new OrganizationMember();
            member.setId(toLong(m.get("id")));
            member.setOrganizationId(toLong(m.get("organizationId")));
            member.setUserId(toLong(m.get("userId")));
            member.setRole((String) m.get("role"));
            member.setNicknameInOrg((String) m.get("nicknameInOrg"));
            return member;
        }).toList();
    }

    @Override
    @Transactional
    public OrganizationMember addMember(Long orgId, Long userId, String role, String nickname) {
        OrganizationMember existing = memberMapper.findByOrgAndUser(orgId, userId);
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                throw new RuntimeException("用户已是该组织成员");
            }
            existing.setStatus(1);
            existing.setRole(role != null ? role : "member");
            memberMapper.updateById(existing);
            return existing;
        }
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(orgId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "member");
        member.setNicknameInOrg(nickname);
        member.setStatus(1);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional
    public void removeMember(Long orgId, Long userId) {
        OrganizationMember member = memberMapper.findByOrgAndUser(orgId, userId);
        if (member == null) throw new RuntimeException("用户不是该组织成员");
        if ("owner".equals(member.getRole())) throw new RuntimeException("不能移除组织所有者");
        memberMapper.deleteByOrgAndUser(orgId, userId);
    }

    @Override
    public OrganizationMember updateMemberRole(Long orgId, Long userId, String role) {
        OrganizationMember member = memberMapper.findByOrgAndUser(orgId, userId);
        if (member == null) throw new RuntimeException("用户不是该组织成员");
        if ("owner".equals(member.getRole())) throw new RuntimeException("不能修改所有者角色");
        member.setRole(role);
        memberMapper.updateById(member);
        return member;
    }

    @Override
    public List<Map<String, Object>> searchUsersNotInOrg(Long orgId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            keyword = "";
        }
        return memberMapper.searchUsersNotInOrg(orgId, keyword);
    }

    @Override
    public List<OrganizationInvite> getInvites(Long orgId) {
        return inviteMapper.findByOrgId(orgId);
    }

    @Override
    @Transactional
    public OrganizationInvite createInvite(Long orgId, Long inviterId, String role, String inviteeUsername) {
        String code = generateInviteCode();
        LocalDateTime expireTime = LocalDateTime.now().plusDays(7);
        OrganizationInvite invite = new OrganizationInvite();
        invite.setOrganizationId(orgId);
        invite.setInviterId(inviterId);
        invite.setInviteCode(code);
        invite.setInviteeUsername(inviteeUsername);
        invite.setRole(role != null ? role : "member");
        invite.setStatus("pending");
        invite.setExpireTime(expireTime);
        inviteMapper.insert(invite);
        return invite;
    }

    @Override
    @Transactional
    public Map<String, Object> joinByInviteCode(String inviteCode, Long userId) {
        OrganizationInvite invite = inviteMapper.findByCode(inviteCode);
        if (invite == null) throw new RuntimeException("邀请码无效或已过期");
        if (invite.getExpireTime().isBefore(LocalDateTime.now())) {
            invite.setStatus("expired");
            inviteMapper.updateById(invite);
            throw new RuntimeException("邀请码已过期");
        }
        addMember(invite.getOrganizationId(), userId, invite.getRole(), null);
        invite.setStatus("accepted");
        inviteMapper.updateById(invite);
        Organization org = organizationMapper.selectById(invite.getOrganizationId());
        Map<String, Object> result = new HashMap<>();
        result.put("organization", org);
        result.put("role", invite.getRole());
        return result;
    }

    @Override
    public boolean isMember(Long orgId, Long userId) {
        return memberMapper.findByOrgAndUser(orgId, userId) != null;
    }

    @Override
    public boolean isOwner(Long orgId, Long userId) {
        OrganizationMember member = memberMapper.findByOrgAndUser(orgId, userId);
        return member != null && "owner".equals(member.getRole());
    }

    @Override
    public boolean isAdmin(Long orgId, Long userId) {
        OrganizationMember member = memberMapper.findByOrgAndUser(orgId, userId);
        return member != null && ("admin".equals(member.getRole()) || "owner".equals(member.getRole()));
    }

    @Override
    public Long getCurrentOrgId(Long userId) {
        List<OrganizationMember> memberships = memberMapper.findByUserId(userId);
        return memberships.isEmpty() ? null : memberships.get(0).getOrganizationId();
    }

    @Override
    public Map<String, Object> getOrgStats(Long orgId) {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> members = memberMapper.findByOrgIdWithDetails(orgId);
        stats.put("totalMemberCount", members.size());
        
        long ownerCount = members.stream().filter(m -> "owner".equals(m.get("role"))).count();
        long adminCount = members.stream().filter(m -> "admin".equals(m.get("role"))).count();
        long normalMemberCount = members.stream().filter(m -> "member".equals(m.get("role"))).count();
        
        stats.put("ownerCount", ownerCount);
        stats.put("adminCount", adminCount);
        stats.put("normalMemberCount", normalMemberCount);
        
        Organization org = organizationMapper.selectById(orgId);
        stats.put("organization", org);
        
        return stats;
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
