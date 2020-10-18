package com.chm.headlines.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Set;

@Service
public class JedisAdapter implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(JedisAdapter.class);

    private JedisPool pool = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("localhost", 6379);
    }

//    private Jedis getJedis() {            //这个方法容易忘记关闭jedisPool的连接，导致redis卡死，暂弃用
//        return pool.getResource();
//    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
//            return getJedis().get(key);   //这个方法容易忘记关闭jedisPool的连接，导致redis卡死，暂弃用
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();          //一定一定一定要记得关闭jedisPool的连接！！否则很容易导致redis卡死！
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setex(String key, int seconds, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long incr(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incr(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String spop(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.spop(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<String> sdiff(String key1, String key2) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sdiff(key1, key2);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);     //这里jedis.sismember(key, value)的返回值是int(1或0)，那作为当前方法的返回值是Boolean类型？
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> srandmember(String key, int count) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srandmember(key, count);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long zadd(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zadd(key, score, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<String> zrevrange(String key, long start, long stop) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrevrange(key, start, stop);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return .0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setObject(String key, Object object) {      //将对象以JSON字符串的形式存储在Redis中（序列化）
        set(key, JSON.toJSONString(object));
    }

    public <T> T getObject(String key, Class<T> clazz) {       //将存储在Redis中的JSON字符串的形式的对象取出，反序列化为原来的对象
        String value = get(key);
        if (value != null) {
            return JSON.parseObject(value, clazz);
        }
        return null;
    }


    public static void print(int index, Object obj) {
        System.out.println(String.format("%d, %s", index, obj.toString()));
    }

    public static void mainx1(String[] args) {
        Jedis jedis = new Jedis();
        jedis.flushAll();

        //get, set
        jedis.set("hello", "world");
        print(1, jedis.get("hello"));
        jedis.rename("hello", "newHello");
        print(2, jedis.get("newHello"));
        jedis.setex("hello2", 15, "world2");
        print(2, jedis.ttl("hello2"));

        //数值操作
        jedis.set("pv", "100");
        jedis.incr("pv");
        print(2, jedis.get("pv"));
        jedis.incrBy("pv", 5);
        print(2, jedis.get("pv"));

        // 列表操作, 最近来访, 粉丝列表，消息队列
        String listName = "list";
        for (int i = 0; i < 10; i++) {
            jedis.lpush("listName", "a" + String.valueOf(i));
        }
        print(3, jedis.lrange("listName", 0, 12));
        print(4, jedis.llen("listName"));
        print(5, jedis.lpop("listName"));   //lpop()的返回值即为pop出的那个元素
        print(6, jedis.llen("listName"));
        print(7, jedis.lindex("listName", 3));
        //注意下面 linsert() 的语法与Redis官网的命令有区别 (ListPosition.BEFORE)
        print(8, jedis.linsert("listName", ListPosition.BEFORE, "a3", "xx"));
        print(9, jedis.linsert("listName", ListPosition.AFTER, "a3", "yy"));
        print(10, jedis.lrange("listName", 0, 12));

        //hash, 可以方便地增删（新）字段
        String userKey = "user00";
        jedis.hset(userKey, "name", "jim");
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "11122223333");
        print(12, jedis.hget(userKey, "name"));
        print(13, jedis.hgetAll(userKey));      //hash表中的kv对按什么顺序输出呢？
        jedis.hdel(userKey, "phone");
        print(14, jedis.hgetAll(userKey));
        print(15, jedis.hexists(userKey, "name"));
        print(16, jedis.hexists(userKey, "email"));
        print(17, jedis.hkeys(userKey));
        print(18, jedis.hvals(userKey));
        jedis.hset(userKey, "name", "cm");      //覆盖原来的value
        jedis.hset(userKey, "age", "24");
        jedis.hsetnx(userKey, "girlFriend", "py");      //仅当 "girlFriend" 不存在时，才添加此字段
        jedis.hsetnx(userKey, "age", "11");            // "age" 字段已存在，故跳过这个操作
        print(19, jedis.hgetAll(userKey));

        // 集合，点赞用户群, 共同好友
        String likeKey1 = "newsLike1";
        String likeKey2 = "newLikes2";
        for (int i = 0; i < 10; i++) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i * 2));
        }
        print(20, jedis.smembers(likeKey1));
        print(21, jedis.smembers(likeKey2));
        print(22, jedis.sunion(likeKey1, likeKey2));    //并集
        print(23, jedis.sinter(likeKey1, likeKey2));    //交集 (intersection)
        print(24, jedis.sdiff(likeKey1, likeKey2));     //差集 (difference set)
        jedis.srem(likeKey1, "5");
        print(25, jedis.sismember(likeKey1, "12"));
        print(26, jedis.sismember(likeKey2, "12"));
        print(27, jedis.smembers(likeKey1));
        jedis.smove(likeKey2, likeKey1, "12");      // SMOVE (sourceSet, destinationSet, member)
        print(28, jedis.smembers(likeKey1));
        print(29, jedis.scard(likeKey1));       //card(A)表示集合A中元素的个数

        // 排序集合，有限队列，排行榜
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "Jim");
        jedis.zadd(rankKey, 60, "Ben");
        jedis.zadd(rankKey, 90, "Lee");
        jedis.zadd(rankKey, 75, "Lucy");
        jedis.zadd(rankKey, 80, "Mei");
        //zcard()即获取有序集的元素个数
        print(30, jedis.zcard(rankKey));
        print(31, jedis.zcount(rankKey, 61, 100));
        print(32, jedis.zscore(rankKey, "Lucy"));
        //为集合中某一个元素加分（也可以减分），返回值为加（或减）之后score的新值
        print(33, jedis.zincrby(rankKey, 3, "Lucy"));
        print(33, jedis.zscore(rankKey, "Lucy"));
        //若zincrby()中的member不存在，则在集合中新增该元素，该元素值即为该increment(初值看作0, 再加上increment)
        print(34, jedis.zincrby(rankKey, 2, "Luc"));
        print(35, jedis.zcount(rankKey, 0, 100));   //上面的"Luc"也已经添加到集合中，score为2
        print(36, jedis.zrange(rankKey, 0, 10));    //zrevrange()取出对应索引(index)范围的值（区别于zrangeByScore() )
        print(36, jedis.zrange(rankKey, 1, 3));     //start索引是从0开始
        print(36, jedis.zrevrange(rankKey, 1, 3));  //取出倒数第二至第4的元素
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, 0, 100)) {   //Tuple
            print(37, tuple.getElement() + ": " + tuple.getScore());
        }

        print(38, jedis.zrank(rankKey, "Ben"));
        print(39, jedis.zrevrank(rankKey, "Ben"));

        JedisPool pool = new JedisPool();
        for (int i = 0; i < 100; i++) {
            Jedis j = pool.getResource();
            j.get("a");
            System.out.println("POOL" + i);
//            j.close();        //若每次只申请且不关闭该线程，则最多只能神器难道8个线程；若每次申请且在用完后关闭该线程，则可无限进行下去
        }
    }
}
