package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeStep;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeStepMapper extends BaseMapper<RecipeStep> {

    @Select("SELECT * FROM recipe_step WHERE recipe_id = #{recipeId} ORDER BY step_number")
    List<RecipeStep> findByRecipeId(Long recipeId);

    @Delete("DELETE FROM recipe_step WHERE recipe_id = #{recipeId}")
    void deleteByRecipeId(Long recipeId);
}
