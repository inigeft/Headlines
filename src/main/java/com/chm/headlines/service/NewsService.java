package com.chm.headlines.service;

import com.chm.headlines.dao.CacheHitDao;
import com.chm.headlines.dao.NewsCacheDAO;
import com.chm.headlines.dao.NewsDAO;
import com.chm.headlines.model.News;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.RedisKeyUtil;
import com.chm.headlines.util.Score;
import com.chm.headlines.util.ToutiaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class NewsService {
    @Autowired
    private NewsDAO newsDAO;

    @Autowired
    NewsService newsService;

    @Autowired
    NewsCacheDAO newsCacheDAO;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    CacheHitDao cacheHitDao;

    private final String USER_NEWS_MAPPING_PRE = "newsCreatedByUser:";

    public List<News> getLatestNews(int userId, int offset, int limit) {
        return newsDAO.selectByUserIdAndOffset(userId, offset, limit);
    }

    public List<News> getMostPopularNews(int userId, int offset, int limit) {

        List<News> newsList = new ArrayList<>();

        //若userId不为0，表示查看某user发布的news列表
        if (userId != 0) {
            //为每个user维护了一个他发布的所有news的newsId的集合，在需要获取该user发布的若干条news时，直接从该集合中批量取（另见NewsService.addNews()方法）
            List<String> newsOfASpecificUser = jedisAdapter.srandmember(USER_NEWS_MAPPING_PRE + userId, limit);
            for (String newsId : newsOfASpecificUser) {
                newsList.add(getById(Integer.parseInt(newsId)));
            }
        } else {
            Set<String> newsIds = jedisAdapter.zrevrange(RedisKeyUtil.MOST_POPULAR_NEWS_RANKING, offset, offset + limit - 1);
            for (String newsId : newsIds) {
                newsList.add(getById(Integer.parseInt(newsId)));
            }
        }
        return newsList;
    }


    public int addNews(News news) {
        newsDAO.addNews(news);
        newsCacheDAO.addNewsToCache(news);

        //为每个user维护一个他发布的所有news的newsId的集合，在需要获取该user发布的若干条news时，直接从该集合中批量取
        jedisAdapter.sadd(USER_NEWS_MAPPING_PRE + news.getUserId(), String.valueOf(news.getId()));

        jedisAdapter.zadd(RedisKeyUtil.MOST_POPULAR_NEWS_RANKING, Score.updateAndGetScore(news.getId()), String.valueOf(news.getId()));
        jedisAdapter.sadd(RedisKeyUtil.ALL_NEWS, String.valueOf(news.getId()));
//        jedisAdapter.sadd(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE, String.valueOf(news.getId()));

        return news.getId();
    }

    public News getById(int newsId) {
        //若要取的news在 "newsNeedToUpdateScore" 集合中，说明缓存中该news的数据已过时，应直接从数据库中取
        if(jedisAdapter.sismember(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE,String.valueOf(newsId))) {
            News news = newsDAO.getById(newsId);
            newsCacheDAO.addNewsToCache(news);
            return news;
        }else {
            News news = newsCacheDAO.getNewsFromCache(newsId);
            if (news != null) {
                cacheHitDao.hit();      //记录缓存命中率，便于之后优化redis性能
            } else {
                cacheHitDao.miss();
                news = newsDAO.getById(newsId);
                newsCacheDAO.addNewsToCache(news);
            }
            return news;
        }
    }

    public String saveImage(MultipartFile file) throws IOException {
        int dotPos = file.getOriginalFilename().lastIndexOf(".");
        if (dotPos < 0) {
            return null;
        }
        String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase(); //记得toLowerCase()
        if (!ToutiaoUtil.isFileAllowed(fileExt)) {
            return null;
        }

        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
        Files.copy(file.getInputStream(), new File(ToutiaoUtil.IMG_DIR + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);

        return ToutiaoUtil.TOUTIAO_DOMAIN + "image?name=" + fileName;
    }

    public int updateCommentCount(int id, int count) {
        return newsDAO.updateCommentCount(id, count);
    }

    public int updateLikeCount(int id, int likeCount) {
        return newsDAO.updateLikeCount(id, likeCount);
    }
}
