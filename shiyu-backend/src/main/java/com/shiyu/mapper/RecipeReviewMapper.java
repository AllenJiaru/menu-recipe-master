package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.RecipeReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecipeReviewMapper extends BaseMapper<RecipeReview> {

    @Select("SELECT * FROM recipe_review WHERE status = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<RecipeReview> findPending();
}
