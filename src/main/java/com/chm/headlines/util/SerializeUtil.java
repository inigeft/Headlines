package com.chm.headlines.util;

import com.chm.headlines.model.News;

public class SerializeUtil {

    private static final String separator = "/////";

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
}
