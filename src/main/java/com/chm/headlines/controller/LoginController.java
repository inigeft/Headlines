package com.chm.headlines.controller;

import com.chm.headlines.async.EventModel;
import com.chm.headlines.async.EventProducer;
import com.chm.headlines.async.EventType;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.service.UserService;
import com.chm.headlines.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);    //这里应使用org.slf4j的Logger类，勿错选成用其他来源的Logger类

    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.GET, RequestMethod.POST})    //@RequestMapping 定义入口
    @ResponseBody
    public String reg(Model model,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam(value = "rember", defaultValue = "0") int remeberme,
                      HttpServletResponse response) {

        try {
            Map<String, Object> map = userService.register(username, password);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");        //设置该cookie为全站有效
                response.addCookie(cookie);
                if (remeberme != 0) {
                    //下面setMaxAge()方法的单位是秒（见API文档http://tomcat.apache.org/tomcat-5.5-doc/servletapi/index.html)
                    cookie.setMaxAge(3600 * 24 * 5);
                }
                return ToutiaoUtil.getJSONString(0, "注册成功");
            } else {
                return ToutiaoUtil.getJSONString(1, map);
            }
        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "注册异常");
        }
    }

    @RequestMapping(path = {"/login/"}, method = {RequestMethod.GET, RequestMethod.POST})    //@RequestMapping 定义入口
    @ResponseBody
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "rember", defaultValue = "0") int remeberme,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            //一个用户每次登录后，都会被下发一个新的ticket（我发现牛客网也是这样），为什么要这样做呢？？

          if (map.containsKey("ticket")) {      //ticket也可以看作token，含义一样，都是表示用户身份的概念（可翻译为“令牌”）
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");        //设置该cookie为全站有效
                response.addCookie(cookie);
                if (remeberme > 0) {
                    //下面setMaxAge()方法的单位是秒（见API文档http://tomcat.apache.org/tomcat-5.5-doc/servletapi/index.html)
                    cookie.setMaxAge(3600 * 24 * 5);
                }

                //发送登录事件到EventProducer，随后异步生成并处理相关事件
//              eventProducer.fireEvent(new EventModel(EventType.LOGIN).setActorId((int) map.get("userId"))
//                      .setExt("userName", username).setExt("email", "cm@qq.com"));

              eventProducer.fireEvent(new
                      EventModel(EventType.LOGIN).setActorId((int) map.get("userId"))
                      .setExt("username", "xx").setExt("to", "yy@yy.com"));     //xx,yy处需自行配置收件人邮箱及用户名

                return ToutiaoUtil.getJSONString(0, "登录成功");
            } else {
                return ToutiaoUtil.getJSONString(1, map);
            }
        } catch (Exception e) {
            logger.error("登录异常" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "登录异常");
        }
    }

    @RequestMapping(path = {"/logout/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return"redirect:/";
    }
}
