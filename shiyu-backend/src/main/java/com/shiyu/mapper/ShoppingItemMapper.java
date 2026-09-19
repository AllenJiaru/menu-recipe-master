package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.ShoppingItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingItemMapper extends BaseMapper<ShoppingItem> {

    @Select("SELECT * FROM shopping_item WHERE user_id = #{userId} AND list_name = #{listName} AND deleted = 0 ORDER BY is_checked, category, ingredient_name")
    List<ShoppingItem> findByUserAndList(@Param("userId") Long userId, @Param("listName") String listName);
}
