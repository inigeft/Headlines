package com.chm.headlines;

import com.chm.headlines.model.User;
import com.chm.headlines.util.JedisAdapter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JedisTest  {

    @Autowired
    JedisAdapter jedisAdapter;

    @Test
    public void testJedis() {
        User user = new User();
        user.setName("userxx");
        user.setPassword("pwd");
        user.setHeadUrl("http://images.nowcoder.com/head/100t.png");
        user.setSalt("def");
        jedisAdapter.setObject("userxx", user);

        User u = jedisAdapter.getObject("userxx", User.class);
        System.out.print(ToStringBuilder.reflectionToString(u));
    }
}
