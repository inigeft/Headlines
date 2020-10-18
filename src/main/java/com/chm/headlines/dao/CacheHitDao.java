package com.chm.headlines.dao;

import com.chm.headlines.util.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 读写缓存命中率数据
 **/
@Component
public class CacheHitDao {

    @Autowired
    JedisAdapter jedisAdapter;

    private static final String CACHE_HIT_KEY = "cache-hit";
    private static final String CACHE_MISS_KEY = "cache-miss";

    public void hit() {
        jedisAdapter.incr(CACHE_HIT_KEY);
    }

    public void miss() {
        jedisAdapter.incr(CACHE_MISS_KEY);
    }
}
