package com.chm.headlines.controller;

import com.chm.headlines.model.EntityType;
import com.chm.headlines.model.HostHolder;
import com.chm.headlines.model.News;
import com.chm.headlines.model.ViewObject;
import com.chm.headlines.service.LikeService;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    private List<ViewObject> getNews(int userId, int offset, int limit) {
//        List<News> newsList = newsService.getLatestNews(userId, offset, limit);
        List<News> newsList = newsService.getMostPopularNews(userId, offset, limit);
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<ViewObject> vos = new ArrayList<>();
        for (News news : newsList) {
            ViewObject vo = new ViewObject();
            vo.set("news", news);
            vo.set("user", userService.getUser(news.getUserId()));
            /* 上面不能写成set("users",……)，因为home.html中对应的user-info(line 147)中是以user表示vo中的用户字段名，若这里写成users,
             * vo中用户信息键值对（ViewObject实际上是用map存储的，见model）对应的key就为"users"，与后面home.html中的user-info
             * 调用user的字段名"user"不匹配，则会导致实际生成的网页中每条news右边无法显示用户头像和用户名 */

            //根据当前已登录的用户对每条news的赞/踩状态，选择是否高亮显示赞/踩按钮
            if (localUserId != 0) {
                //根据该用户是否是该news的like(或dislike)集合中的元素，判断该用户当前对该news的赞/踩状态
                vo.set("like", likeService.getLikeStatus(EntityType.ENTITY_NEWS, news.getId(), localUserId));
            } else {
                vo.set("like", 0);
            }
            vos.add(vo);
        }
        return vos;
    }

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})    //@RequestMapping 定义入口
    public String index(Model model,
                        @RequestParam(value = "pop", defaultValue = "0") int pop) {
        model.addAttribute("vos", getNews(0, 0, 10));     //通过将vos添加到model的属性，使得在velocity中（即src\main\resources\templates\home.html中）也能访问vos中的数据
        if (hostHolder.getUser() != null) {
            pop = 0;
        }
        model.addAttribute("pop", pop);
        return "home";
    }

    /*{已解决}
    最终仍有一个bug没有找到，访问首页时，右侧user的头像等信息全部没有显示，且点击首页的每条news右侧用户头像出现错误页
    （原本应该跳转到这个用户发的所有news的页面）；同时，直接访问网址"127.0.0.1:8080/user/11"则能正常跳转显示userId=11的用户发出的所有news*/

    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})    //path = {"/user/{userId}/ "}（多了'\'和一个空格）
    //RequestMapping的地址一定要每一个字符都正确，否则访问很可能会出错
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos", getNews(userId, 0, 10));
        return "home";
    }
    /*{已解决}
    仍然存在的问题：
    无法登陆已注册的账户*/

    @RequestMapping(path = {"/ajax/news"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String getNewsWithAjax(Model model, @RequestParam(value = "pop", defaultValue = "0") int pop, @RequestParam(value = "offset") int offset) {
        model.addAttribute("vos", getNews(0, offset, 10));
        if (hostHolder.getUser() != null) {
            pop = 0;
        }
        model.addAttribute("pop", pop);
        return "GetNewsWithAjax";
    }}
