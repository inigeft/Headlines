package com.chm.headlines;

import com.chm.headlines.dao.CommentDAO;
import com.chm.headlines.dao.LoginTicketDAO;
import com.chm.headlines.dao.NewsDAO;
import com.chm.headlines.dao.UserDAO;
import com.chm.headlines.model.*;
import com.chm.headlines.service.CommentService;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.util.JedisAdapter;
import com.chm.headlines.util.Score;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@Sql({"/init-schema.sql"})
public class InitDatabaseTests {

	@Autowired
	NewsDAO newsDAO;		//会自动关联到NewsDAO自动创建的对象
							//语句以;结束，勿漏
	@Autowired
	UserDAO userDAO;

	@Autowired
	LoginTicketDAO loginTicketDAO;

	@Autowired
	CommentDAO commentDAO;

	@Autowired
	JedisAdapter jedisAdapter;

	@Autowired
	CommentService commentService;

	@Autowired
	NewsService newsService;

	@Test
	public void InitDataBase() {
		Random r = new Random();
		for (int i = 0; i < 11; i++) {
			User user = new User();
			user.setName(String.format("USER%d", i));
			user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", r.nextInt(1000)));
			user.setPassword("");
			user.setSalt("");
			userDAO.addUser(user);

			News news = new News();
			news.setCommentCount(i);
			Date date = new Date();
			date.setTime(date.getTime() + 1000 * 3600 * 5 * i);
			news.setCreatedDate(date);
			news.setImage(String.format("http://images.nowcoder.com/head/%dm.png", r.nextInt(1000)));
			news.setLikeCount(i + 1);
			news.setLink(String.format("http://www.nowcoder.com/%d.html", i));
			news.setTitle(String.format("TITLE{%d}", i));
			news.setUserId(i + 1);
			newsDAO.addNews(news);

			//给每条资讯插入3条评论
			for (int j = 0; j < 3; j++) {
				Comment comment = new Comment();
				comment.setUserId(i + 1);
				comment.setContent("Comment " + String.valueOf(j));
				comment.setCreatedDate(new Date());
				comment.setStatus(0);
				comment.setEntityType(EntityType.ENTITY_NEWS);
				comment.setEntitytId(news.getId());
				commentDAO.addComment(comment);
				//更新news里的评论数量，以后用异步实现
				int count = commentService.getCommentCount(EntityType.ENTITY_NEWS, news.getId());     //这里EntityId 即为 newsId
				newsService.updateCommentCount(news.getId(), count);
			}

			//更新这条资讯用于排序的score（应该移到likeServise.like()里更新的，但是需要重构一部分代码，所以先这样写，可以以后再改）
			jedisAdapter.zadd("mostPopularNewsRanking", Score.updateAndGetScore(news.getId()), String.valueOf(news.getId()));

			//打印每一条资讯的分数
			System.out.printf("%d-分数：%f%n", news.getId(), jedisAdapter.zscore("mostPopularNewsRanking", String.valueOf(news.getId())));

			user.setPassword("newpassword");
			userDAO.updatePassword(user);

			LoginTicket loginTicket = new LoginTicket();
			loginTicket.setUserId(i + 1);
			loginTicket.setExpired(date);
			loginTicket.setStatus(0);
			loginTicket.setTicket(String.format("TICKET%d", i + 1));
			loginTicketDAO.addLoginTicket(loginTicket);

			loginTicketDAO.updateStatus(2, loginTicket.getTicket());
		}

//		List<News> news = new LinkedList<>();
//		Set<String> newsIds = jedisAdapter.zrevrange("mostPopularNewsRanking", 0, 10 - 1);
//		for (String newsId : newsIds) {
//			System.out.println(newsId);
//			news.add(newsService.getById(Integer.parseInt(newsId)));
//		}

		System.out.printf("%d%n", jedisAdapter.zcard("mostPopularNewsRanking"));

		Assert.assertEquals("newpassword", userDAO.selectById(1).getPassword());	//断言
		userDAO.deleteById(1);
		Assert.assertNull(userDAO.selectById(1));	//断言，若user表中id为1的记录部位不为空，则运行测试用例时会报错

		Assert.assertEquals(1, loginTicketDAO.selectByTicket("TICKET1").getUserId());
		Assert.assertEquals(2, loginTicketDAO.selectByTicket("TICKET1").getStatus());

		Assert.assertNotNull(commentDAO.selectByEntity(EntityType.ENTITY_NEWS, 1).get(1));
	}

}

