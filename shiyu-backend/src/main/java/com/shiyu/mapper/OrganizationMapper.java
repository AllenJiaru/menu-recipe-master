package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
    
    @Select("SELECT * FROM organization WHERE deleted = 0 ORDER BY sort_order, id")
    List<Organization> findAll();
    
    @Select("SELECT * FROM organization WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order, id")
    List<Organization> findByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT * FROM organization WHERE type = #{type} AND deleted = 0 ORDER BY sort_order, id")
    List<Organization> findByType(@Param("type") String type);
    
    @Select("SELECT * FROM organization WHERE deleted = 0 AND (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) ORDER BY sort_order, id")
    List<Organization> search(@Param("keyword") String keyword);
}
