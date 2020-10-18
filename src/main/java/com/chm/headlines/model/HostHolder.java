package com.chm.headlines.model;

import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();
    /*为了避免同一用户的信息同时被多处访问时发生相互干扰，设置ThreadLocal变量userThreadLocal，即每个线程有自己独立的userThreadLocal*/

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
