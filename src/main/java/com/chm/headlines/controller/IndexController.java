package com.chm.headlines.controller;

import com.chm.headlines.aspect.LogAspect;
import com.chm.headlines.model.User;
import com.chm.headlines.service.ToutiaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

//@Controller         //用于注明下面的IndexController类为controller
public class IndexController {
    public static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired      //Ioc
    private ToutiaoService toutiaoService;     //控制反转（loc）(依赖注入）

   // @RequestMapping(path = {"/", "/index"})     //@RequestMapping 定义入口
    @ResponseBody
    public String index(HttpSession session,
                        HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (Cookie cookie : request.getCookies()) {        //这一段输出cookie的for-each循环是我自己尝试巩固之前所学的
            builder.append("Cookie: ");
            builder.append(cookie.getName());
            builder.append(": ");
            builder.append(cookie.getValue());
            builder.append("<br>");
        }
        logger.info("Visit Index");
        return "Hello World" + session.getAttribute("tips") + "<br>" + builder.toString() + toutiaoService.say();
    }

    @RequestMapping(path = {"/profile/{groupId}/{userId}"})
    @ResponseBody
    public String profile(@PathVariable("groupId") String groupId,
                          @PathVariable("userId") int userId,
                          @RequestParam(value = "type", defaultValue = "1") int type,
                          @RequestParam(value = "key", defaultValue = "coder") String key) {
        return String.format("GID{%s}, UID{%s}, TYPE{%d}, KEY{%s}", groupId, userId, type, key);
    }

    @RequestMapping(value = {"/vm"})
    public String news(Model model) {       /* 这里的model是后端（如这里的IndexController)和
      渲染（如src\main\resources\templates\news.vm)之间交联的存储数据的模型（即可在两者之间共享、传递数据）（视频课ch2.24:30) */
        model.addAttribute("value1", "viva");
        List<String> colors = Arrays.asList(new String[]{"RED", "GREEN", "BLUE"});

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            map.put(String.valueOf(i), String.valueOf(i * i));

            model.addAttribute("colors", colors);
            model.addAttribute("map", map);     /*这里的第一个参数"map"是何意？把这里的"map"换成
                                                    其他字符串如"numbers"程序就无法正常运行*/
            model.addAttribute("User", new User("Jim"));
            model.addAttribute("date", new Date().getTime());

        }
        return "news";
    }

    @RequestMapping(value = {"/request"})
    @ResponseBody
    public String request(HttpServletRequest request,
                          HttpServletResponse response,
                          HttpSession session) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            builder.append(name + ":" + request.getHeader(name) + "<br>");
        }

        for (Cookie cookie : request.getCookies()) {
            builder.append("Cookie:");
            builder.append(cookie.getName());
            builder.append(":");
            builder.append(cookie.getValue());
            builder.append("<br>");
        }

        builder.append("getMethod: " + request.getMethod() + "<br>");
        builder.append("getPathInfo: " + request.getPathInfo() + "<br>");
        builder.append("getQueryString: " + request.getQueryString() + "<br>");
        builder.append("getRequestURI: " + request.getRequestURI() + "<br>");

        return builder.toString();
    }

    @RequestMapping(value = {"/response"})
    @ResponseBody
    public String response(@CookieValue(value = "nowcoder", defaultValue = "a") String nowcoderId,
                           @RequestParam(value = "value", defaultValue = "value") String value,
                           @RequestParam(value = "key", defaultValue = "key") String key,
                           HttpServletResponse response) {
        response.addCookie(new Cookie(key, value));
        response.addHeader(key, value);     //为什么这里之前nowcoderID没有response回去？在这里直接加上nowcoderID不行？
        //为什么访问 /request?key=nowcoder&&value=22 就能把nowcoderId传过去并由下面一行显示出来？
        return "NowcoderId From Cookie: " + nowcoderId;
    }

    /*  @RequestMapping("/redirect/{code}")
      public RedirectView redirect(@PathVariable("code") int code) {
          RedirectView red = new RedirectView("/", true);
          if (code == 301) {
              red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
          }
          return red;*/
    @RequestMapping("/redirect/{code}")
    public String redirect(@PathVariable("code") int code,
                           HttpSession session) {       //这里须理解一个session的含义
        session.setAttribute("tips", ", Jump from redirect");
        return "redirect:/";        //这里通过redirect前缀指明要跳转到哪里
    }                               //相对于上面的301/302（已注释），这种方法更简单直接。同时，下面这种方法只能是302跳转

    @RequestMapping("/admin")
    @ResponseBody
    public String admin(@RequestParam(value = "key", required = false) String password) {
        //上面的"key"是指用户访问时链接应为/admin?key=xxxx的形式，而非/admin?password=xxxx
        if ("admin".equals(password)) {
            return "Hello admin!";
        }
        throw new IllegalArgumentException("wrong key!");
    }

    @ExceptionHandler
    @ResponseBody
    public String error(Exception e) {
        return "Oops!  " + e.getMessage();
    }
}
















