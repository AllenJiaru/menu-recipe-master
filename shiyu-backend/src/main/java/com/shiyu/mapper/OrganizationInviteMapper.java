package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.OrganizationInvite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrganizationInviteMapper extends BaseMapper<OrganizationInvite> {
    
    @Select("SELECT * FROM organization_invite WHERE invite_code = #{code} AND status = 'pending' LIMIT 1")
    OrganizationInvite findByCode(@Param("code") String code);
    
    @Select("SELECT * FROM organization_invite WHERE organization_id = #{orgId} ORDER BY create_time DESC")
    List<OrganizationInvite> findByOrgId(@Param("orgId") Long orgId);
}
