package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.AiConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfigEntity> {
    @Select("SELECT config_value FROM ai_config WHERE config_key = #{key}")
    String getValueByKey(@Param("key") String key);
}
