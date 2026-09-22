package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.AiFeatureLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AiFeatureLogMapper extends BaseMapper<AiFeatureLog> {

    @Select("<script>" +
            "SELECT * FROM ai_feature_log WHERE deleted = 0 " +
            "<if test='userId != null'>AND user_id = #{userId}</if>" +
            "<if test='feature != null and feature != \"\"'>AND feature = #{feature}</if>" +
            " ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<AiFeatureLog> findLogs(@Param("userId") Long userId, @Param("feature") String feature,
                                @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM ai_feature_log WHERE deleted = 0 " +
            "<if test='userId != null'>AND user_id = #{userId}</if>" +
            "<if test='feature != null and feature != \"\"'>AND feature = #{feature}</if>" +
            "</script>")
    int countLogs(@Param("userId") Long userId, @Param("feature") String feature);

    @Update("UPDATE ai_feature_log SET related_id = #{relatedId}, related_type = #{relatedType} WHERE id = #{id} AND deleted = 0")
    int updateRelated(@Param("id") Long id, @Param("relatedId") Long relatedId, @Param("relatedType") String relatedType);
}
