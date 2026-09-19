package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.MealPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealPlanMapper extends BaseMapper<MealPlan> {

    @Select("SELECT * FROM meal_plan WHERE user_id = #{userId} AND plan_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY plan_date, meal_type")
    List<MealPlan> findByDateRange(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
