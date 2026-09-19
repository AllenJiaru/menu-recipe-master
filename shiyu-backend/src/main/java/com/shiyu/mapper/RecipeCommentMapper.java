package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeCommentMapper extends BaseMapper<RecipeComment> {

    @Select("SELECT * FROM recipe_comment WHERE recipe_id = #{recipeId} AND parent_id = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<RecipeComment> findTopLevelByRecipe(@Param("recipeId") Long recipeId);
}
