package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
    @Select("<script>" +
            "SELECT * FROM ai_chat_session WHERE deleted = 0 " +
            "<if test='userId != null'>AND user_id = #{userId}</if>" +
            "<if test='userId == null'>AND user_id IS NULL</if>" +
            " ORDER BY update_time DESC" +
            "</script>")
    List<AiChatSession> findByUserId(@Param("userId") Long userId);
}
