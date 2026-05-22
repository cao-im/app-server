package com.caoim.appserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caoim.appserver.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<AppUser> {

    @Select("SELECT * FROM app_user WHERE username = #{username} AND deleted = 0")
    AppUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM app_user WHERE id = #{id} AND deleted = 0")
    AppUser selectById(@Param("id") Long id);
}
