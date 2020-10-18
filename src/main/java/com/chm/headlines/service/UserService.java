package com.chm.headlines.service;

import com.chm.headlines.dao.LoginTicketDAO;
import com.chm.headlines.dao.UserDAO;
import com.chm.headlines.model.LoginTicket;
import com.chm.headlines.model.User;
import com.chm.headlines.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    public static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    public Map<String, Object> register(String username, String password) {
        Map<String, Object> map = new HashMap<>();

        //验证注册的用户名是否为空
        if (StringUtils.isBlank(username)) {
            map.put("msgname", "用户名不能为空");
            return map;
        }

        //验证注册使用的密码是否为空
        if (StringUtils.isBlank(password)) {
            map.put("msgpwd", "密码不能为空");
            return map;
        }

        //验证注册使用的密码字符是否少于6位
        if (StringUtils.length(password) < 8) {
            map.put("msgpwd", "密码长度不能少于8位");
            return map;
        }

        //验证注册使用的密码是否仅包含数字
        if (StringUtils.isNumeric(password)) {
            map.put("msgpwd", "密码必须同时包含字母和数字");
            return map;
        }

        //验证注册的用户名是否已被注册
        if (userDAO.selectByName(username) != null) {
            //上面的判断条件不能写成"userDAO.selectByName(username).getName()!= null"，否则在LoginController会捕获到null异常
            map.put("msgname", "该用户名已被注册");
            return map;
        }

        User user = new User();
        user.setName(username);
        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeadUrl(head);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        user.setPassword(ToutiaoUtil.MD5(password + user.getSalt()));
        userDAO.addUser(user);

        String ticket = addLoginTicket(user.getId());   //通常新用户注册成功后即会自动以注册好的账号登录，故这里注册成功后即给用户下发ticket
        map.put("ticket", ticket);

        return map;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<>();

        //验证登录的用户名是否为空
        if (StringUtils.isBlank(username)) {
            map.put("msgname", "用户名不能为空");
            return map;
        }

        //验证登录的密码是否为空
        if (StringUtils.isBlank(password)) {
            map.put("msgpwd", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);
        //验证登录的用户是否存在
        if (user == null) {
            map.put("msgname", "用户名不存在");
            return map;
        }

        //验证登录的密码是否匹配
        if (!ToutiaoUtil.MD5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("msgpwd", "密码不正确");
            return map;
        }

        map.put("userId", user.getId());

        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);

        return map;
    }

    public void logout(String ticket) {
        loginTicketDAO.updateStatus(1, ticket);
    }

    private String addLoginTicket(int userId) {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 3600 * 24);    // ticket 的过期时间设为24小时
        loginTicket.setExpired(date);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addLoginTicket(loginTicket);

        return loginTicket.getTicket();
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }


}
