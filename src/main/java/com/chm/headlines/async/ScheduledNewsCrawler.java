package com.chm.headlines.async;

import com.chm.headlines.service.NewsCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledNewsCrawler {

    @Autowired
    NewsCrawlerService newsCrawlerService;

    @Scheduled(fixedRate = 1800000)   //每30分钟抓取一次新闻
    public void scheduledFillNewsFromCrawler() {
        newsCrawlerService.fillNewsFromCrawler();
    }
}
