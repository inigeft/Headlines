package com.chm.headlines.controller;

import com.chm.headlines.model.*;
import com.chm.headlines.service.*;
import com.chm.headlines.util.ToutiaoUtil;
import com.chm.headlines.util.XSSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class NewsController {
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    QiniuService qiniuService;

    @Autowired
    AliOssService aliOssService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    MessageService messageService;

    @Autowired
    LikeService likeService;

    @RequestMapping(path = {"/news/{newsId}"}, method = RequestMethod.GET)   //这里不是RequestMethod.POST
    public String newsDetail(@PathVariable("newsId") int newsId, Model model) {     //这里是@PathVariable，勿写成@RequestVariable !
        News news = newsService.getById(newsId);
        if (news != null) {
            int localUserID = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
            //根据当前已登录的用户对每条news的赞/踩状态，选择是否高亮显示赞/踩按钮
            if (localUserID != 0) {
                //根据该用户是否是该news的like(或dislike)集合中的元素，判断该用户当前对该news的赞/踩状态
                model.addAttribute("like", likeService.getLikeStatus(EntityType.ENTITY_NEWS, newsId, localUserID));
            } else {
                model.addAttribute("like", 0);
            }
            List<Comment> comments = commentService.getCommentByEntity(EntityType.ENTITY_NEWS, newsId);
            List<ViewObject> commentVOs = new ArrayList<>();
            for (Comment comment : comments) {
                ViewObject commentVO = new ViewObject();
                commentVO.set("comment", comment);
                commentVO.set("user", userService.getUser(comment.getUserId()));
                commentVOs.add(commentVO);
            }
            model.addAttribute("comments", commentVOs);
        }
        model.addAttribute("news", news);
        model.addAttribute("owner", userService.getUser(news.getUserId()));
        return "detail";
    }



    @RequestMapping(path = {"/addComment"}, method = RequestMethod.POST)
    public String addComment(@RequestParam("newsId") int newsId,
                             @RequestParam("content") String content) {
        try {
            //过滤content中的敏感词汇（高级项目课内容）

            Comment comment = new Comment();
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setEntitytId(newsId);
            comment.setUserId(hostHolder.getUser().getId());
            comment.setContent(content);

            commentService.addComment(comment);

            //更新news里的评论数量，以后用异步实现
            int count = commentService.getCommentCount(EntityType.ENTITY_NEWS, newsId);     //EntityId 即为 newsId ?
            newsService.updateCommentCount(newsId, count);

        } catch (Exception e) {
            logger.error("提交评论异常" + e.getMessage());
        }
        return "redirect:/news/" + String.valueOf(newsId);
    }

    @RequestMapping(path = {"/user/addNews/"}, method = RequestMethod.POST)
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link) {
        try {
            News news = new News();
            news.setImage(image);
            news.setTitle(XSSFilter.filterBrackets(title));
            news.setLink("热点资讯_%_"+link);
            news.setCreatedDate(new Date());

            if (hostHolder.getUser() != null) {
                news.setUserId(hostHolder.getUser().getId());
            } else {
                //设置一个匿名用户，可自己重新定义
                news.setUserId(1);
            }
            newsService.addNews(news);
            return ToutiaoUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("添加资讯失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "发布失败");
        }
    }


    @RequestMapping(path = {"/uploadImage/"}, method = RequestMethod.POST)
    @ResponseBody
    public String uploadImage(@RequestParam("file") MultipartFile file) {   //@RequestParam中的两个file分别代表什么？
        try {
            //最初使用七牛云的对象存储服务
//            String fileUrl = newsService.saveImage(file);
            //也建议熟悉方法file.transferTo()的使用
//            String fileUrl = qiniuService.savaImage(file);

            //改为使用阿里云的对象存储服务
            String fileUrl = aliOssService.savaImage(file);


            if (fileUrl == null) {
                return ToutiaoUtil.getJSONString(1, "上传图片失败");
            }
            return ToutiaoUtil.getJSONString(0, fileUrl);
        } catch (Exception e) {
            logger.error("上传图片识别" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "上传图片失败");
        }
    }

    @RequestMapping(path = {"/image"}, method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");
            StreamUtils.copy(new FileInputStream(new File(ToutiaoUtil.IMG_DIR + imageName)), response.getOutputStream());
        } catch (Exception e) {
            logger.error("读取图片错误" + imageName + e.getMessage());
        }
    }

}
