package com.chm.headlines.dao;

import com.chm.headlines.model.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper     // @Mapper表示这里的UserDAO与数据库中的user表是一一匹配的(所以操控UserDAO也就相当于操控数据库中的user表？）
public interface LoginTicketDAO {

    String TABLE_NAME = "login_ticket";
    String INSERT_FIELDS = " user_id, expired, status, ticket ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;       //" id, "中的逗号勿漏！！！

    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS, ") values (#{userId}, #{expired}, #{status}, #{ticket})"})
    int addLoginTicket(LoginTicket loginTicket);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);

    @Update({"Update ", TABLE_NAME, " set status=#{status} where ticket=#{ticket}"})
    void  updateStatus(@Param("status") int status,
                      @Param("ticket") String ticket);
}