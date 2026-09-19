package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
    @Select("SELECT * FROM ai_chat_message WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY create_time ASC")
    List<AiChatMessage> findBySessionId(@Param("sessionId") Long sessionId);
}
