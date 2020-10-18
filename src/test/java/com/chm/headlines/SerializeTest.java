package com.chm.headlines;

import com.alibaba.fastjson.JSON;
import com.chm.headlines.model.News;
import com.chm.headlines.util.SerializeUtil;
import java.io.*;
import java.util.Date;

/**
 * 用于比较各种序列化的时间和空间
 **/
public abstract class SerializeTest
{
    public static void main(String[] args)
    {
        System.out.println("Test1_result");     //Text1测试自定义序列化方式性能
        Test1 test1 = new Test1();
        test1.test();
        System.out.println("\nTest2_result");     //Text2测试Java自带序列化方式性能
        Test2 test2 = new Test2();
        test2.test();
        System.out.println("\nTest3_result");     //Text3测试FastJson序列化方式性能
        Test2 test3 = new Test2();
        test2.test();
    }

    public void test()
    {
        News news = new News();
        news.setId(1);
        news.setUserId(1);
        news.setTitle("test测试test测试test测试test测试test测试");
        news.setImage("http://testtesttesttest.com/test/test");
        news.setLink("http://testtesttesttest.com/test/test");
        news.setCreatedDate(new Date());
        news.setLikeCount(100);
        news.setCommentCount(100);

        int times = 1000000;
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            byte[] bytes = templateMethod1(news);
            if (i == 0) {
                System.out.println("space: " + bytes.length);
            }
            templateMethod2(bytes);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("time: " + (endTime - currentTime) / 1000.0 + "s");
    }

    abstract byte[] templateMethod1(News news);

    abstract void templateMethod2(byte[] s);
}

class Test1 extends SerializeTest
{
    @Override
    byte[] templateMethod1(News news)
    {
        return SerializeUtil.writeNewsObject(news).getBytes();
    }

    @Override
    void templateMethod2(byte[] s)
    {
        SerializeUtil.readNewsObject(new String(s));
    }
}

class Test2 extends SerializeTest
{
    @Override
    byte[] templateMethod1(News news)
    {
        return writeNewsObject(news);
    }

    @Override
    void templateMethod2(byte[] s)
    {
        readNewsObject(s);
    }

    private byte[] writeNewsObject(News news)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(news);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    private News readNewsObject(byte[] bytes)
    {
        News news= null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            news = (News) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return news;
    }

}

class Test3 extends SerializeTest
{
    @Override
    byte[] templateMethod1(News news)
    {
        return writeNewsObject(news);
    }

    @Override
    void templateMethod2(byte[] s)
    {
        readNewsObject(s);
    }

    private byte[] writeNewsObject(News news)
    {
        return JSON.toJSONString(news).getBytes();
    }

    private News readNewsObject(byte[] bytes)
    {
        News news = JSON.parseObject(bytes, News.class);

        return news;
    }
}
