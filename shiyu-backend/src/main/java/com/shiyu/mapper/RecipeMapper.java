package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.Recipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {

    @Select("SELECT * FROM recipe WHERE sync_id = #{syncId}")
    Recipe findBySyncId(String syncId);

    @Select("SELECT * FROM recipe WHERE update_time > #{lastSyncTime} AND deleted = 0")
    List<Recipe> findModifiedAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}
