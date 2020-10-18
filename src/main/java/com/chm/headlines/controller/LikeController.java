package com.chm.headlines.controller;

import com.chm.headlines.async.EventModel;
import com.chm.headlines.async.EventProducer;
import com.chm.headlines.async.EventType;
import com.chm.headlines.model.EntityType;
import com.chm.headlines.model.HostHolder;
import com.chm.headlines.service.LikeService;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.ToutiaoUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    NewsService newsService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    JedisAdapter jedisAdapter;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String like(@Param("newId") int newsId) {        //{已解决} 这里应该用@RequestParam(no)（见项目课问题记录11）
        int userId = hostHolder.getUser().getId();
        long likeCount = likeService.like(EntityType.ENTITY_NEWS, newsId, userId);
        newsService.updateLikeCount(newsId, (int) likeCount);

//        //更新这条资讯用于排序的score（应该移到likeServise.like()里更新的，但是需要重构一部分代码，所以先这样写，可以以后再改）
//        jedisAdapter.zadd("mostPopularNewsRanking", Score.updateAndGetScore(newsId), String.valueOf(newsId));


        /*
         * 考虑到消费事件时通常需要该事件的现场数据，因此 EventModel 中的 setActorId、setEntityId 等方法的返回值均为 this,
         * 即返回修改后的该对象，因此在新建 EventModel 对象的同时可以直接设置各属性，更方便
         */
        eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.getUser().getId())
                .setEntityId(newsId).setEntityType(EntityType.ENTITY_NEWS)
                .setEntityOwnerId(newsService.getById(newsId).getUserId()));

        return ToutiaoUtil.getJSONString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String dislike(@Param("newId") int newsId) {
        int userId = hostHolder.getUser().getId();
        long likeCount = likeService.dislike(EntityType.ENTITY_NEWS, newsId, userId);
        newsService.updateLikeCount(newsId, (int) likeCount);

//        //更新这条资讯的分数（应该移到likeServise.like()里更新的，但是需要重构一部分代码，所以先这样写，可以以后再改）
//        jedisAdapter.zadd("mostPopularNewsRanking", Score.updateAndGetScore(newsId), String.valueOf(newsId));

        return ToutiaoUtil.getJSONString(0, String.valueOf(likeCount));
    }


    //页面上点赞/踩时，IDE的Console中会报错，但暂时未发现其他影响，暂不清楚原因，先实现完后面的代码再说
}
