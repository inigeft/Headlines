package com.chm.headlines.async;

import com.chm.headlines.dao.NewsCacheDAO;
import com.chm.headlines.dao.NewsDAO;
import com.chm.headlines.model.EntityType;
import com.chm.headlines.model.News;
import com.chm.headlines.service.CommentService;
import com.chm.headlines.service.LikeService;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.RedisKeyUtil;
import com.chm.headlines.util.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ScheduledScoreUpdater {

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    LikeService likeService;

    @Autowired
    NewsService newsService;

    @Autowired
    CommentService commentService;

    @Autowired
    NewsDAO newsDAO;

    @Autowired
    NewsCacheDAO newsCacheDAO;

    //对于近期有过点赞、点踩、评论的news，每5分钟更新一次它们的score，这里为了调试将更新周期设为1秒
    @Scheduled(fixedRate = 1000)        //fixedRate默认单位是毫秒
    public void scheduledUpdatePopularNewsScore() {
//        System.out.println("scheduledUpdatePopularNewsScore" + new Date());
        while (jedisAdapter.scard(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE) != 0) {
            //在newsNeedToUpdate集合中，随机取出一个news并更新其score
            int newsId = Integer.parseInt(jedisAdapter.spop(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE));
            double theNewScore = Score.updateAndGetScore(newsId);
            jedisAdapter.zadd(RedisKeyUtil.MOST_POPULAR_NEWS_RANKING, theNewScore, String.valueOf(newsId));

            //更新这些news在数据库中的likeCount
            String likeKey = RedisKeyUtil.getLikeKey(EntityType.ENTITY_NEWS, newsId);
            long likeCount = jedisAdapter.scard(likeKey);
            newsService.updateLikeCount(newsId, (int) likeCount);

            //更新这些news在数据库中的commentCount
            int count = commentService.getCommentCount(EntityType.ENTITY_NEWS, newsId);
            newsService.updateCommentCount(newsId, count);

            //更新缓存中的这条news

            //去数据库中完整地取出这条news的最新信息，并加入缓存中
            /*
              这里更好的做法是反序列化redis缓存的这条news，修改需要改的字段后，再序列化后存到redis缓存中
              这样能尽可能降低数据库的IO压力
            */
            News news = newsDAO.getById(newsId);
            newsCacheDAO.addNewsToCache(news);
        }
    }

    /*
    对于近期没有点赞、点踩、评论的news，每30分钟更新一次它们score，这里为了调试将更新周期设为2秒
    （主要是随着时间流逝，这些非热门news的score需要下调，但是上面的scheduledUpdatePopularNewsScore方法仅能更新热门news的score
    */
    @Scheduled(fixedRate = 2000)
    public void scheduledUpdateUnpopularNewsScore() {
        /*
        "allNews"集合中包括所有news（每个news刚新建就加入了这个集合，见newsService.addNews()方法）,
         它与"NewsNeedToUpdateScore"集合的差集就是UnpopularNews集合
        */
        Set<String> unpopularNewsSet = jedisAdapter.sdiff(RedisKeyUtil.ALL_NEWS, RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE);

        //更新这些unpopularNews在mostPopularNewsRanking有序集合中的分数（mostPopularNewsRanking集合包含所有的newsId以及它们的score）
        for (String newsId : unpopularNewsSet) {
            double theNewScore = Score.updateAndGetScore(Integer.parseInt(newsId));
            jedisAdapter.zadd(RedisKeyUtil.MOST_POPULAR_NEWS_RANKING, theNewScore, newsId);
        }
    }
}
