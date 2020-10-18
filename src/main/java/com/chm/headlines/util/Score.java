package com.chm.headlines.util;

import com.chm.headlines.model.News;
import com.chm.headlines.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;
import java.util.Date;

@Component
public class Score {

    private static NewsService newsService;

    private static double score;

    @Autowired
    public Score(NewsService newsService) {
        Score.newsService = newsService;
    }

//    public Double getScore() {
//        return score;
//    }

    public static double updateAndGetScore(int newsId) {
        News news = newsService.getById(newsId);
        int likeCount = news.getLikeCount();
        int commentCount = news.getCommentCount();
        Date createdDate = news.getCreatedDate();
        double hoursSinceCreated = (new Date().getTime() - createdDate.getTime()) / 1000 / 3600;

        //        /*下面注释中是无谓操作，已弃用
//        分子上的1000000是将score的小数点右移。原来score值太小（如0.00073964497041420117），在最右边有很多位数无法显示出来，
//        并且double型的score没有利用好小数点左边的位数（double的有效位数是16位）
//        */
//        score = (likeCount * Math.pow(commentCount + 1, 2) + 1) *100000 / (hoursSinceCreated + 1);

        //在java中 double 型的有效位数就是16，不论数值多大或多小，永远会保留16位有效数字，不会因为数值过大或过小而损失精度
        score = (likeCount + 5 * commentCount + 1)  / Math.pow ((hoursSinceCreated + 2), 2);

        return score;
    }
}
