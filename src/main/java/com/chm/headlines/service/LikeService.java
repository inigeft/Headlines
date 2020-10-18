package com.chm.headlines.service;

import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    NewsService newsService;

    public int getLikeStatus(int entityType, int entityId, int userId) {

        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return jedisAdapter.sismember(dislikeKey, String.valueOf(userId)) ? -1 : 0;
    }

    public long like(int entityType, int entityId, int userId) {

        //如果调用like方法的是news(而不是comment)，则更新mostPopularNewsRanking的有序队列中该news的分数
        //通过entityType判断是news还是comment
//        if(entityType == EntityType.ENTITY_NEWS) {
//            jedisAdapter.zadd(RedisKeyUtil.KEY_OF_MOST_POPULAR_NEWS_RANKING, Score.updateAndGetScore(entityId), String.valueOf(entityId));
//        }


        /*
        如果调用like方法的是news(而不是comment)，则将该news的newsId放入一个set中（这个set中的news都在近期发生过被点赞/踩/评论，
        因此需要更新它们的score，会有一个定期执行的线程更新这个set中的news的score
        */
            jedisAdapter.sadd(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE, String.valueOf(entityId));

        // 在喜欢集合里增加
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));

        // 从反对集合里删除
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.srem(dislikeKey, String.valueOf(userId));

        //返回当前喜欢的人数
        return jedisAdapter.scard(likeKey);
    }

    public long dislike(int entityType, int entityId, int userId) {

//        //如果调用like方法的是news(而不是comment)，则更新mostPopularNewsRanking的有序队列中该news的分数
//        //通过entityType判断是news还是comment
//        if(entityType == EntityType.ENTITY_NEWS) {
//            jedisAdapter.zadd(RedisKeyUtil.KEY_OF_MOST_POPULAR_NEWS_RANKING, Score.updateAndGetScore(entityId), String.valueOf(entityId));
//        }

        /*
        如果调用like方法的是news(而不是comment)，则将该news的newsId放入一个set中（这个set中的news都在近期发生过被点赞/踩/评论，
        因此需要更新它们的score，会有一个定期执行的线程更新这个set中的news的score
        */
            jedisAdapter.sadd(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE, String.valueOf(entityId));

        // 在反对集合里增加
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.sadd(dislikeKey, String.valueOf(userId));

        // 从喜欢集合里删除
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));

        //返回当前喜欢的人数
        return jedisAdapter.scard(likeKey);
    }
}
