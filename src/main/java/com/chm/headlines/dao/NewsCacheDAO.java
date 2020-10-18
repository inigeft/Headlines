package com.chm.headlines.dao;

import com.chm.headlines.model.News;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.SerializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class NewsCacheDAO {

    @Autowired
    JedisAdapter jedisAdapter;

    private final String NewsCachePre = "news:";


    public void addNewsToCache(News news) {
        String key = NewsCachePre + news.getId();
        jedisAdapter.set(key, SerializeUtil.writeNewsObject(news));     //采用自定义序列化方式

    }

    public News getNewsFromCache(int newsId) {
        News news = new News();
        String key = NewsCachePre + newsId;
        String s = jedisAdapter.get(key);
        //缓存中可能没有缓存当前news，因此上一步get(key)结果可能为空，因此下面需要判空
        if(s != null) {
            news = SerializeUtil.readNewsObject(s);
        }
        return news;
    }
}
