package com.chm.headlines.service;

import com.chm.headlines.dao.CommentDAO;
import com.chm.headlines.model.Comment;
import com.chm.headlines.model.EntityType;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    Logger logger = LoggerFactory.getLogger(CommentService.class);


    @Autowired
    NewsService newsService;

    @Autowired
    CommentDAO commentDAO;

    @Autowired
    JedisAdapter jedisAdapter;

    public int addComment(Comment comment) {
        int result = commentDAO.addComment(comment);

//        //更新这条资讯用于排序的score
//        jedisAdapter.zadd("mostPopularNewsRanking", Score.updateAndGetScore(comment.getEntitytId()), String.valueOf(comment.getEntitytId()));

        /*
        如果调用like方法的是news(而不是comment)，则将该news的newsId放入一个set中（这个set中的news都在近期发生过被点赞/踩/评论，
        因此需要更新它们的score，会有一个定期执行的线程更新这个set中的news的score
        */
        jedisAdapter.sadd(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE, String.valueOf(comment.getEntitytId()));

        //更新news里的评论数量，以后用异步实现
        int count = getCommentCount(EntityType.ENTITY_NEWS, comment.getEntitytId());
        newsService.updateCommentCount(comment.getEntitytId(), count);

        return result;
    }

    public List<Comment> getCommentByEntity(int entityType, int entityId) {
        return commentDAO.selectByEntity(entityType, entityId);
    }

    public int getCommentCount(int entityType, int entityId) {
        return commentDAO.getCommentCount(entityType, entityId);
    }

    void deleteComment(int entityType, int entityId) {
        commentDAO.updateStates(entityType, entityId, 1);

        // 这里的deleteComment()暂时没有使用，使用更新这条资讯用于排序的score的语句暂时先不写（上面addComment()的更新score语句已经写好了）
    }
}
