package com.chm.headlines.util;

import com.chm.headlines.model.News;
import com.chm.headlines.service.CommentService;
import com.chm.headlines.service.LikeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class JsoupAdapter {

    @Autowired
    LikeService likeService;

    @Autowired
    CommentService commentService;

    public List<News> getPopularNewsWithCrawler() throws IOException {

//        URL url = new URL("https://www.thepaper.cn/");
//        // 获得连接
//        URLConnection connection = url.openConnection();
//        connection.setRequestProperty("Referer", "https://www.thepaper.cn/");
//        Connection.Response response

//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");

        //从澎湃新闻爬取最新资讯
        String siteUrl = "https://www.thepaper.cn/";
        String linkUrlPrefix = "https://www.thepaper.cn/";

//        //若网页是动态渲染的，则需要用到这里注释掉的htmlunit
//        WebClient webClient = new WebClient(BrowserVersion.CHROME);
//        webClient.getOptions().setUseInsecureSSL(true);
//        webClient.getOptions().setJavaScriptEnabled(true);
//        webClient.getOptions().setCssEnabled(false);
//        webClient.getOptions().setActiveXNative(false);
//        webClient.getOptions().setCssEnabled(false);
//        webClient.getOptions().setThrowExceptionOnScriptError(false);
//        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
//        webClient.getOptions().setTimeout(10000);
//        webClient.getCurrentWindow().setInnerHeight(Integer.MAX_VALUE);
//        HtmlPage htmlPage = null;
////        try {
//            htmlPage = webClient.getPage(siteUrl);
//            webClient.waitForBackgroundJavaScript(10000);
//            String pageAsXml = htmlPage.asXml();
////            return Jsoup.parse(htmlString);
////        } finally {
////            webClient.close();
////        }


        Document document = Jsoup.connect(siteUrl)
////                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36")
////                .header("","https://www.thepap er.cn/")
////                .cookie("UM_distinctid","17350165c5613e-05a713ae9226c9-4353760-e1000-17350165c5727b")
                .get();
        Element newsbox = document.getElementById("masonryContent");

        Elements news_li = newsbox.getElementsByClass("news_li");
        List<News> PopularNews = new ArrayList<>();
        for (Element eachNews : news_li) {
            String title = eachNews.getElementsByTag("h2").eq(0).text();
            //澎湃新闻的新闻列表中，掺杂有若干空新闻，在这里过滤空新闻
            if (title.isEmpty()) {
                continue;
            }
            String link = linkUrlPrefix + eachNews.getElementsByTag("a").eq(0).attr("href");
            String image = eachNews.getElementsByTag("img").attr("src");
            //linkSourceName是澎湃新闻每条资讯的发布者的名字
            String linkSourceName = eachNews.getElementsByTag("a").eq(2).text();
            News news = new News();
            news.setTitle(title);

            //这里的"_%_"是自定义的分隔符，用来在velocity模板中使用split('_%_').get(index)取回linkSourceName和link
            news.setLink(linkSourceName + "_%_" + link);

            news.setImage(image);

            news.setCreatedDate(new Date());
            /*
            预设userId=1的用户为管理员，用户名为admin，这里假设抓取的新闻都由管理员发布，即news.userId=1
            admin的密码预设为"password"
             */
            news.setUserId(1);
            news.setCommentCount(0);
            PopularNews.add(news);

        }
        return PopularNews;
    }


//    public static void main(String[] args) throws MalformedURLException {
//        JsoupAdapter jsoupAdapter = new JsoupAdapter();
//        try {
//            jsoupAdapter.getPopularNewsWithCrawler();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}

