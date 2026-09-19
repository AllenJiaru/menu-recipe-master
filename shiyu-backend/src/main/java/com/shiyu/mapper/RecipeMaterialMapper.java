package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeMaterial;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeMaterialMapper extends BaseMapper<RecipeMaterial> {

    @Select("SELECT * FROM recipe_material WHERE recipe_id = #{recipeId}")
    List<RecipeMaterial> findByRecipeId(Long recipeId);

    @Delete("DELETE FROM recipe_material WHERE recipe_id = #{recipeId}")
    void deleteByRecipeId(Long recipeId);
}
