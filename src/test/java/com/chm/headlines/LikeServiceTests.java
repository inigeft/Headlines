package com.chm.headlines;

import com.chm.headlines.service.LikeService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LikeServiceTests {

    @Autowired
    LikeService likeService;

    @Test
    public void testLike() {
        likeService.like(10, 1, 1);
        Assert.assertEquals(1, likeService.getLikeStatus(10, 1, 1));
    }

    @Test
    public void testDislike() {
        likeService.dislike(12, 1, 1);
        Assert.assertEquals(-1, likeService.getLikeStatus(12, 1, 1));
    }


    @Test(expected = IllegalArgumentException.class)        //异常测试
    public void testException() {
        throw new IllegalArgumentException("异常");
    }


    @Before
    public void setUp() {
        System.out.println("setUp");
    }

    @After
    public void tearDown() {
        System.out.println("tearDown");
    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }
}
