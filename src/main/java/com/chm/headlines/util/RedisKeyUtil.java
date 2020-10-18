package com.chm.headlines.util;

public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENT = "EVENT";

    public static String ALL_NEWS = "allNews";
    public static String MOST_POPULAR_NEWS_RANKING= "mostPopularNewsRanking";
    public static String NEWS_NEED_TO_UPDATE_SCORE= "newsNeedToUpdateScore";
    public static String CRAWLED_NEWS= "crawledNews";


    public static String getEventQueueKey() {
        return BIZ_EVENT;
    }

    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDisLikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

}
