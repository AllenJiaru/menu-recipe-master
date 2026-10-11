package com.shiyu.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.entity.SysRole;
import com.shiyu.entity.User;
import com.shiyu.mapper.SysPermissionMapper;
import com.shiyu.mapper.SysRoleMapper;
import com.shiyu.mapper.SysUserRoleMapper;
import com.shiyu.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserDetailsImpl(user, buildAuthorities(user));
    }

    private List<GrantedAuthority> buildAuthorities(User user) {
        Set<String> result = new LinkedHashSet<>();

        // 角色来源：sys_user_role（仅启用状态），回退到 sys_user.role 兼容旧数据
        List<String> roleCodes = sysUserRoleMapper.findActiveRoleCodes(user.getId());
        if (roleCodes.isEmpty() && user.getRole() != null && !user.getRole().isBlank()) {
            roleCodes = List.of(user.getRole());
        }
        for (String roleCode : roleCodes) {
            result.add("ROLE_" + roleCode.toUpperCase());
        }

        boolean isAdmin = roleCodes.stream()
                .anyMatch(rc -> "admin".equalsIgnoreCase(rc) || "super_admin".equalsIgnoreCase(rc));

        Set<String> permCodes = new HashSet<>();
        if (isAdmin) {
            // 管理员拥有全部启用权限（含新增权限）
            permCodes.addAll(sysPermissionMapper.findAllActiveCodes());
        } else {
            permCodes.addAll(sysPermissionMapper.findActiveCodesByUserId(user.getId()));
            // 兼容旧单角色字段回退
            if (permCodes.isEmpty() && user.getRole() != null && !user.getRole().isBlank()) {
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRole::getCode, user.getRole());
                wrapper.eq(SysRole::getStatus, 1);
                SysRole role = sysRoleMapper.selectOne(wrapper);
                if (role != null) {
                    sysPermissionMapper.findByRoleId(role.getId())
                            .forEach(p -> permCodes.add(p.getCode()));
                }
            }
            // 前缀继承：拥有粗粒度码（目录/菜单）即拥有其下全部细粒度码
            if (!permCodes.isEmpty()) {
                List<String> allCodes = sysPermissionMapper.findAllActiveCodes();
                List<String> inherited = new ArrayList<>();
                for (String code : allCodes) {
                    for (String granted : permCodes) {
                        if (code.startsWith(granted + ":")) {
                            inherited.add(code);
                            break;
                        }
                    }
                }
                permCodes.addAll(inherited);
            }
        }
        result.addAll(permCodes);

        List<GrantedAuthority> authorities = new ArrayList<>(result.size());
        for (String authority : result) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        return authorities;
    }
}
