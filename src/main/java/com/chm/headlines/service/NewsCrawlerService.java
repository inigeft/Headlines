package com.chm.headlines.service;

import com.chm.headlines.dao.NewsCacheDAO;
import com.chm.headlines.model.News;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.JsoupAdapter;
import com.chm.headlines.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsCrawlerService {

    @Autowired
    private NewsService newsService;

    @Autowired
    LikeService likeService;

    @Autowired
    CommentService commentService;

    @Autowired
    private JedisAdapter jedisAdapter;

    @Autowired
    private JsoupAdapter jsoupAdapter;

    @Autowired
    NewsCacheDAO newsCacheDAO;

    public void fillNewsFromCrawler() {
        try {
            List<News> PopularNews = new ArrayList<>();
            PopularNews = jsoupAdapter.getPopularNewsWithCrawler();
            for (News news : PopularNews) {
                /*
                将爬取的新闻简单地去重.由于爬取源目前只有澎湃新闻，因此默认每条新闻的title不会重复。
                由于redis set的存入中文字符需要编码方式，因此用news的link来匹配重复
                将爬取的新闻的link加到名为 "CrawedNews" 的redis set中，每次爬取到的新资讯，
                在写入数据库之前都先验证在名为 "CrawedNews" 的redis set中是否已存在
                 */
                if (!jedisAdapter.sismember(RedisKeyUtil.CRAWLED_NEWS, news.getLink())) {
                    newsService.addNews(news);
                    jedisAdapter.sadd(RedisKeyUtil.CRAWLED_NEWS, news.getLink());

//                    //为了模拟新闻的点赞、评论，以管理员的身份为每条抓取的新闻点赞并添加一条评论
//                    likeService.like(EntityType.ENTITY_NEWS, news.getId(), 1);
//
//                    /*
//                    上面47行将新闻加入数据库后，可能立刻有用户访问该新闻，因此下面不应简单粗暴地将news.likeCount设为1，
//                    否则可能导致数据不一致，这里将其likeCount设为redis中该news的like用户的集合的元素数量
//                     */
//                    String likeKey = RedisKeyUtil.getLikeKey(EntityType.ENTITY_NEWS, news.getId());
//                    newsService.updateLikeCount(news.getId(), (int) jedisAdapter.scard(likeKey));
//                    /*
//                    用下面这条语句则无法完成likeCount更新，因为此时news的属性已经持久化到数据库中，无法通过news类的set方法
//                    修改数据库中的值，需要调用newsService.updateLikeCount->newsDAO.updateLikeCount，通过sql语句操纵数据库
//                    news.setLikeCount((int) jedisAdapter.scard(likeKey));
//                     */
//
//                    Comment comment = new Comment();
//                    comment.setContent("commentFromAdmin");
//                    comment.setCreatedDate(new Date());
//                    comment.setUserId(1);
//                    comment.setStatus(0);
//                    comment.setEntityType(EntityType.ENTITY_NEWS);
//                    comment.setEntitytId(news.getId());
//                    commentService.addComment(comment);
//
//                    int commentCount = commentService.getCommentCount(EntityType.ENTITY_NEWS, news.getId());
//                    newsService.updateCommentCount(news.getId(), commentCount);
//
//                    newsCacheDAO.addNewsToCache(news);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
