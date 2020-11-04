<div align="center">
    <h1>
        Headlines
    </h1>
</div>

* [项目简述](#%E9%A1%B9%E7%9B%AE%E7%AE%80%E8%BF%B0)
* [在线演示](#%E5%9C%A8%E7%BA%BF%E6%BC%94%E7%A4%BA)
* [运行环境](#%E8%BF%90%E8%A1%8C%E7%8E%AF%E5%A2%83)
* [性能测试](#%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95)
* [缓存实现](#%E7%BC%93%E5%AD%98%E5%AE%9E%E7%8E%B0)
  * [业务需求](#%E4%B8%9A%E5%8A%A1%E9%9C%80%E6%B1%82)
  * [Redis配置](#redis%E9%85%8D%E7%BD%AE)
  * [实现](#%E5%AE%9E%E7%8E%B0)
  * [序列化方式的选择](#%E5%BA%8F%E5%88%97%E5%8C%96%E6%96%B9%E5%BC%8F%E7%9A%84%E9%80%89%E6%8B%A9)
* [异步设计](#%E5%BC%82%E6%AD%A5%E8%AE%BE%E8%AE%A1)
  * [自行实现的消息队列](#%E8%87%AA%E8%A1%8C%E5%AE%9E%E7%8E%B0%E7%9A%84%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97)
  * [点击首页底部， 异步加载更多资讯](#%E7%82%B9%E5%87%BB%E9%A6%96%E9%A1%B5%E5%BA%95%E9%83%A8-%E5%BC%82%E6%AD%A5%E5%8A%A0%E8%BD%BD%E6%9B%B4%E5%A4%9A%E8%B5%84%E8%AE%AF)
* [资讯排序算法](#%E8%B5%84%E8%AE%AF%E6%8E%92%E5%BA%8F%E7%AE%97%E6%B3%95)
  * [算法说明](#%E7%AE%97%E6%B3%95%E8%AF%B4%E6%98%8E)
  * [资讯分值的更新](#%E8%B5%84%E8%AE%AF%E5%88%86%E5%80%BC%E7%9A%84%E6%9B%B4%E6%96%B0)
* [安全性](#%E5%AE%89%E5%85%A8%E6%80%A7)
  * [用户密码加 salt 后散列存储](#%E7%94%A8%E6%88%B7%E5%AF%86%E7%A0%81%E5%8A%A0-salt-%E5%90%8E%E6%95%A3%E5%88%97%E5%AD%98%E5%82%A8)
  * [XSS 防御](#xss-%E9%98%B2%E5%BE%A1)


### 项目简述

***

​		“头条资讯”是一个资讯分享与聚合网站。系统的主要功能模块包括：首页推荐、资讯详情、用户登录以及站内信。系统会定时爬取其他新闻网站受欢迎的资讯（标注源新闻地址），同时，用户也可以注册、登录后，自行发布包含图片、文字、转载地址（可选）的资讯。用户可以为每条资讯点赞、点踩、评论，系统会根据资讯的发布时间、点赞数、评论数等因素，定时计算它们的分数（分数代表受欢迎度），并将最受欢迎的若干条资讯展示在首页。特殊事件（例如用户发布的资讯被点赞）发生时，会将该事件加入待处理事件的队列中，系统异步地完成后续操作（如发送站内信提醒用户）。

### 在线演示

***

[头条资讯 Headlines](http://117.78.7.107:8080/)


### 运行环境

***

本系统运行在一台云服务器上。

* CPU: 1 Intel SkyLake 6161 2.2GHz
* 内存：2GB

* 系统：CentOS 7.3 64bit
* MySQL: Ver 8.0.20 
* Tomcat: Ver 8.5.12
* Redis: Ver 6.0.6

### 性能测试

***

使用Apache的ab工具来进行压力测试。

为了排除网络延迟的隐形，因此在服务器端运行ab工具进行测试。

使用以下命令来使用 ab 工具，其中 -c 参数为并发数，-n 参数为请求数，-k 参数表示持久连接，http://localhost:8080/ 就是待测试的网站。

```shell
ab -c 1000 -n 5000 -k http://localhost:8080/
```

在使用Redis进行缓存之前，进行以上测试得到部分结果如下，可以看到每秒请求数为92.8。

```
Time taken for tests: 53.877 seconds
Total transferred: 152265000 bytes
HTML transferred: 151580000 bytes
Requests per second: 92.80 [#/sec] (mean)
```

而在使用Redis作缓存之后，测试结果如下，每秒请求数提高到了145.15，增长了58%，有效提升了系统的吞吐量。

```
Time taken for tests: 34.447 seconds
Total transferred: 160480000 bytes
HTML transferred: 159795000 bytes
Requests per second: 145.15 [#/sec] (mean)
```

### 缓存实现

***

#### 业务需求

资讯内容具有读多写少的特性，为了降低数据库的IO压力、提高系统性能，将热点数据进行缓存是非常合适的做法。本系统使用Redis缓存最受欢迎的若干条资讯。

#### Redis配置

为了将Redis用作缓存，需要进行两方面的配置：内存最大使用量、内存回收策略。

应基于服务器可用内存，以及系统可能用到的最大内存来指定Redis的maxmemory，通常应大于热点数据所占空间。

Redis 有五种缓存淘汰策略，如下表所示：

| 策略            | 描述                                                        |
| --------------- | ----------------------------------------------------------- |
| noeviction      | 禁止驱逐数据                                                |
| allkeys-lru     | 从所有数据集中挑选最近最少使用的数据淘汰                    |
| volatile-lru    | 从已设置过期时间的数据集中挑选最近最少使用的数据淘汰        |
| allkeys-random  | 从所有数据集中随机选择数据淘汰                              |
| volatile-random | 从已设置过期时间的数据集中随机选择数据淘汰                  |
| volatile-ttl    | 从已设置过期时间的数据集中挑选存活时间（TTL）最短的数据淘汰 |

noeviction 不主动淘汰数据，仅在内存用尽且正在进行增加内存使用量的操作时，返回错误信息，显然不适合用作缓存淘汰策略；在 allkeys 中选择数据可能误删费缓存的数据，也不适合缓存系统；random 和基于存活时间（TTL）的策略也不适用，它们都不能在内存中尽可能留存热点数据。LRU（least recently used） 策略将最近最少使用的数据进行淘汰，而最近频繁访问的热点数据得以保留，是最合适的缓存淘汰策略。

在本系统中，Redis在用作缓存系统的同时，还用作存储系统，因此，需要配置两个Redis实例，一个实例用作缓存系统，使用volatile-lru淘汰策略；另一个实例用作存储系统，使用noeviction淘汰策略。

#### 实现

为了实现缓存功能，需要修改获取资讯和添加资讯的代码。

在获取资讯时，先验证缓存中的这条资讯的状态是否已过时（比如该资讯在上次缓存到Redis之后有新的评论、新的点赞等），若未过时且在Redis中有这条资讯的缓存，则从缓存中返回该资讯，否则就从数据库中获取。

在 NewsCacheDAO 中实现了缓存的获取和添加功能，CacheHitDao 用来记录缓存的命中次数和未命中次数，这是为了对系统进行监控，从而对缓存进行优化，并且能够及时发现缓存穿透、缓存击穿和缓存雪崩的问题。

在添加新资讯到数据库的同时，也将它添加到缓存中，这是因为新资讯的分数总是很高（详见资讯排序算法），在添加到数据库后近期被访问的概率很大，为了提高缓存命中率，因此也将它添加到缓存中。

```java
public News getById(int newsId) {
    
    //若要获取的news在 "newsNeedToUpdateScore" 集合中，说明缓存中的该news已过时，应直接从数据库中取
    if(jedisAdapter.sismember(RedisKeyUtil.NEWS_NEED_TO_UPDATE_SCORE,
                              String.valueOf(newsId))) {
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

public int addNews(News news) {
    newsDAO.addNews(news);
    newsCacheDAO.addNewsToCache(news);

    //为每个user维护一个他发布的所有news的newsId的集合，在需要获取该user发布的若干条news时，直接从该集合中批量取
    jedisAdapter.sadd(USER_NEWS_MAPPING_PRE + news.getUserId(), String.valueOf(news.getId()));

    jedisAdapter.zadd(RedisKeyUtil.MOST_POPULAR_NEWS_RANKING, Score.updateAndGetScore(news.getId()), String.valueOf(news.getId()));
    jedisAdapter.sadd(RedisKeyUtil.ALL_NEWS, String.valueOf(news.getId()));

    return news.getId();
}
```

#### 序列化方式的选择

在实现 Redis 缓存功能时，最开始选择使用 Java 自带的序列化方式将一个对象转换成字节数组进行存储，但是这样序列化得到的内容有很多是类定义的内容，这部分内容完全没必要存入缓存中，只需要将几个关键字段拼接成字符串存储即可，实现代码如下：

```java
public static String writeNewsObject(News news) {
    StringBuilder s = new StringBuilder();
    s.append(news.getId()).append(separator);
    s.append(news.getUserId()).append(separator);
    s.append(news.getTitle()).append(separator);
    s.append(news.getImage()).append(separator);
    s.append(news.getLink()).append(separator);
    s.append(DateUtil.formatDate(news.getCreatedDate())).append(separator);
    s.append(news.getLikeCount()).append(separator);
    s.append(news.getCommentCount());

    return s.toString();
}

public static News readNewsObject(String s) {
    News news = new News();
    String[] splitedNewsObject = s.split(separator);
    news.setId(Integer.valueOf(splitedNewsObject[0]));
    news.setUserId(Integer.valueOf(splitedNewsObject[1]));
    news.setTitle(splitedNewsObject[2]);
    news.setImage(splitedNewsObject[3]);
    news.setLink(splitedNewsObject[4]);
    news.setCreatedDate(DateUtil.parseDate(splitedNewsObject[5]));
    news.setLikeCount(Integer.valueOf(splitedNewsObject[6]));
    news.setCommentCount(Integer.valueOf(splitedNewsObject[7]));

    return news;
}
```

类似的，也可以采用 JSON 序列化方式，但是 JSON 格式也会存储各个字段的名称，因此空间开销也应大于字段拼接的序列化方式。

为了验证以上3种序列化方式的时间、空间上的开销，进行了3个基准测试，详细的测试代码在 com/chm/headlines/SerializeTest.java 中。

首先新建并填充一个资讯对象，然后分别使用字符拼接的序列化方式、 Java 自带的序列化方式、JSON 序列化方式进行 1000000 次的序列化和反序列化，统计存储所需要的字节数和总时间，如下表所示：

|                 | 字段拼接 | Java 序列化 | JSON  |
| --------------- | -------- | ----------- | ----- |
| 存储空间 / byte | 186      | 333         | 333   |
| 运行时间 / s    | 5.507    | 9.773       | 9.059 |

可以发现字段拼接方式实现的序列化方式，无论在空间上还是在时间上明显优于其他两种方式，因此本项目采用字段拼接方式来自行实现序列化、反序列化方法。

### 异步设计

***

#### 自行实现的消息队列

为了降低系统各组件间的耦合性，并对流量进行削峰，从而提高系统性能，项目自行实现了一个简易的消息队列，在业务代码中仅需将事件加入消息队列，该事件对应的后续操作将异步完成。该消息队列的结构如下图所示：

<img src="pics\Headlines-消息队列-结构图.png" alt="Headlines-消息队列-结构图"  />

在具体的业务代码（Biz）中，调用 EventProducer 类的 fireEvent() 方法，将事件对象序列化后推入由 Redis Lists 实现的事件队列中，而 EventConsumer 则会阻塞地从事件队列中取出事件，反序列化后交给对应的 EventHandler 执行预定义的操作。

在本项目中，消息队列主要应用在以下两个方面：

1. 某条资讯发生点赞事件时，使用消息队列异步地向该资讯的发布者发送站内信，代码如下：

   ```java
   /*
    * 考虑到消费事件时通常需要该事件的现场数据，因此 EventModel 中的 setActorId、setEntityId 等方法的返回值均为 this,
    * 即返回修改后的该对象，因此在新建 EventModel 对象的同时可以直接设置各属性，更方便
    */
   eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.getUser().getId())
           .setEntityId(newsId).setEntityType(EntityType.ENTITY_NEWS)
           .setEntityOwnerId(newsService.getById(newsId).getUserId()));
   ```

   LikeHandler 中对 like 事件的处理代码如下：

   ```java
   @Override
   public void doHandler(EventModel eventModel) {
       Message message = new Message();
       User user = userService.getUser(eventModel.getActorId());
   
       message.setContent("用户" + user.getName() + "赞了你的资讯 "
                           + ToutiaoUtil.TOUTIAO_DOMAIN + "news/" + String.valueOf(eventModel.getEntityId()));
       
       // 默认 id=1 的用户为系统管理员
       int fromId = 1;
       int toId = eventModel.getEntityOwnerId();
       message.setFromId(fromId);
       message.setToId(toId);
       message.setCreatedDate(new Date());
       message.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
       messageService.addMessage(message);
   }
   ```

2. 用户登录后，异步地向用户发送 “欢迎登陆” 的站内信，实现代码与点赞事件类似。

#### 点击首页底部， 异步加载更多资讯

对于新闻类网站，用户访问网站首页时，完整加载数据库中所有资讯会造成网页加载过慢、数据库IO压力过大等问题，这种做法显然是不可行且不必要的，因此，本项目采用 Ajax 异步加载新资讯并局部刷新网页，以提升用户体验与系统性能。

当用户访问首页时，会加载当前最受欢迎的10条资讯（资讯排序算法请见下一节），当用户浏览完已加载的所有资讯并滚动页面至底部时，点击 “加载更多” 按钮，就会向服务器请求接下来的10条最受欢迎的资讯，并插入当前页面尾部，这样的 “加载更多” 操作可反复进行，直到没有更多资讯可供加载。

### 资讯排序算法

***

本项目参考 Reddit, Hacker News, Stack Overflow 的资讯排序算法的设计思路，综合考虑系统中每条资讯的发布时间、点赞数、评论数，构建出适合本项目的资讯排序算法。系统为每条资讯维护一个分值（score）表示资讯的受欢迎程度，当用户访问网站首页时，按分值降序加载最受欢迎的若干条资讯。

#### 	算法说明

该排序算法如下所示：

<div align=center>
    <img src="pics\资讯排序算法.png" height="48" />
</div>

Score：资讯的分值，为 double 型，分值越大表示越受欢迎

likeCount：资讯的点赞数

commentCount：资讯的评论数

hoursSinceCreated：资讯创建到当前的时间差，单位为小时

该算法主要基于以下几点考虑设计：

1. 由于点赞数、评论数与资讯受欢迎程度都是正相关，因此他们都在分子上，而评论比点赞的操作成本明显更高，因此评论数权重更大
2. 对于新发布的资讯，他们的点赞数、评论数均为0，为了避免它们在计算分值时分子为0，因此分子预先加1
3. 资讯的时效性非常强，因此，发布时间较久的资讯分值应迅速较低，这通过分母的2次幂来实现。分母中加2为了在 hoursSinceCreated 较小时，仍然能保持发布时间差对分值足够大的影响。

#### 资讯分值的更新

资讯的分值会随着点赞、评论、发布时间的变化而变化，因此，及时、高效地更新分值显得尤为重要。

本系统维护以下数据结构来高效地更新资讯分值：

1. 名为 MOST_POPULAR_NEWS_RANKING 的 Redis Zset 保存所有资讯的 ID 及对应的分值。
2. 名为 NEWS_NEED_TO_UPDATE_SCORE 的Redis Set 维护需要尽快更新分值的资讯集合，它们是近期的热点资讯，当资讯发生点赞、评论事件时将加入该集合，系统以较高的频率定期更新集合中所有资讯的分值，资讯分值更新后移出集合。
3. 名为 ALL_NEWS 的 Redis Set 保存系统中所有资讯的 ID。
4. 不在 NEWS_NEED_TO_UPDATE_SCORE 集合中的资讯是冷门资讯。为了体现 hoursSinceCreated 对分值的影响，系统仍然会定期更新它们的分值，但是更新频率会显著低于热点资讯。这些资讯的集合由 ALL_NEWS 与 NEWS_NEED_TO_UPDATE_SCORE  做差运算得到。
5. 上述针对性地高频率更新热点资讯分值，以及低频率更新冷门资讯分值的方式，在保证资讯分值及时更新的同时，也有效节约了系统资源。

### 安全性

***

#### 用户密码加 salt 后散列存储

为了保证用户密码安全，首先需要以正确的方式保存密码。通常有以下三种方式存储密码：	

1. 明文保存

即将用户密码不作任何处理存入数据库，一旦数据库信息泄露，可能造成无法估量的损失，因此这种方式极不安全。

2. 加密保存

即使用密钥将密码加密后，将密文存入数据库。这种方式虽然安全性有提升，但是密钥仍有泄露的风险，进而可能导致密码泄露，这种方式也不安全。

3. 哈希保存

即对密码使用哈希算法加密，将哈希值存入数据库。由于哈希值是单向运算，无法还原，因此即使数据库中的哈希值泄露，安全风险也极低。

然而，利用彩虹表可以对常见密码进行反向查询、破解，仅使用哈希加密仍无法保证用户信息的安全。因此，本系统在存储用户密码 pwd 时，会随机生成一段字符串 salt，将 pwd 与 salt 拼接后字符串的散列值 hash(pwd+salt)  存入数据库（salt 也同时存入），需要验证密码时，将用户输入密码加 salt 散列后与数据库中存储的密文比较验证即可。

#### XSS 防御

对于内容类网站，如果没有对用户发布的内容进行处理，很容易受到 XSS 攻击的影响。例如任何用户都可以发布包含以下代码的内容：

```html
<script> alert("hello"); </script>
```

当用户访问该内容时，就会出现预料之外的弹窗，影响用户体验。

除此之外，XSS还可能被用于以下非法操作：

1. 窃取用户Cookies
2. 劫持流量实现恶意跳转
3. 伪造虚假的表单骗取用户信息

防范 XSS 攻击的也很简单：将 `<` 和 `>` 等字符转义即可。 
