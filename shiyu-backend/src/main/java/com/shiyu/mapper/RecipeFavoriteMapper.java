package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeFavoriteMapper extends BaseMapper<RecipeFavorite> {

    @Select("SELECT * FROM recipe_favorite WHERE user_id = #{userId} AND recipe_id = #{recipeId}")
    RecipeFavorite findUserFavorite(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    @Select("SELECT f.*, r.difficulty, r.cooking_time, c.name AS category_name " +
            "FROM recipe_favorite f " +
            "LEFT JOIN recipe r ON f.recipe_id = r.id " +
            "LEFT JOIN recipe_category c ON r.category_id = c.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<RecipeFavorite> findUserFavoritesWithRecipe(@Param("userId") Long userId);
}
