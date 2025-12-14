package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from sky_take_out.user where openid =#{openid}")
    User getByOpenid(String openid);

    @Insert("insert into sky_take_out.user(openid, name, phone, sex, id_number, avatar, create_time) VALUES" +
            " (#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    void insert(User user);

    Integer getCountByUser(LocalDateTime beginTime, LocalDateTime endTime);

    Integer countByMap(Map map);
}
