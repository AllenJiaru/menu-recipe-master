package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeVersionMapper extends BaseMapper<RecipeVersion> {

    @Select("SELECT * FROM recipe_version WHERE recipe_id = #{recipeId} ORDER BY version DESC")
    List<RecipeVersion> findByRecipe(@Param("recipeId") Long recipeId);
}
