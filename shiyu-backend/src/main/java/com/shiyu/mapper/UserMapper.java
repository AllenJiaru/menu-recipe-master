package com.shiyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    User findByEmail(@Param("email") String email);

    @Select("SELECT * FROM sys_user WHERE (username = #{account} OR email = #{account}) AND deleted = 0")
    User findByUsernameOrEmail(@Param("account") String account);
}
