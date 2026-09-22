package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmailVerificationMapper extends BaseMapper<EmailVerification> {

    @Select("SELECT * FROM email_verification WHERE username = #{username} AND code = #{code} AND purpose = #{purpose} AND used = 0 AND expire_time > NOW() ORDER BY id DESC LIMIT 1")
    EmailVerification findValidCode(@Param("username") String username, @Param("code") String code, @Param("purpose") String purpose);

    @Update("UPDATE email_verification SET used = 1 WHERE id = #{id}")
    int markUsed(@Param("id") Long id);
}
