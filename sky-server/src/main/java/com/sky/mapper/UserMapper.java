package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    void insert(User user);
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据注册时间统计用户数量
     * @param map
     * @return
     */
    Integer sumByMap(Map map);
}
