package com.chm.headlines.interceptor;

import com.chm.headlines.dao.LoginTicketDAO;
import com.chm.headlines.dao.UserDAO;
import com.chm.headlines.model.HostHolder;
import com.chm.headlines.model.LoginTicket;
import com.chm.headlines.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class  PassportInterceptor implements HandlerInterceptor {    //使用implement关键字，实现HandlerInterceptor接口
    //使用快捷键 Ctrl+I(implement method)补全接口

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HostHolder hostHolder;

    @Override
    //在进入controller之前
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals("ticket")) {
                    ticket = cookie.getValue();
                    break;  //后加
                }
            }
        }

        if (ticket != null) {
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
          /*  上面条件的(loginTicket == null)是排除ticket为伪造的情况，那样虽然ticket不为空，
                 但是在数据库中无法有所给ticket找到对应的loginTicket，则loginTicket == null */
                return true;
                //若进入这个if，则说明ticket无效，故返回true结束，相当于什么都没发生过
            }
            /*因为这里的preHandle是发生在进入controller之前，程序进行到这一步时已经知道登录的用户是谁，之后还需要用到该用户的信息，
            但是由于前面验证用户的这些步骤的结果（即在数据库中找到该用户）并没有保存，因此退出preHandler后，若想要获取用户的信息，
            又需要在数据库中重新查找，为了将这里获取到的用户信息保存下来，以方便后续对用户信息的获取，才增加一个model名为HostHolder
            * */
            User user = userDAO.selectById(loginTicket.getUserId());
            hostHolder.setUser(user);
        }
        return true;
    }

    @Override
    //在渲染之前
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    @Override
    //收尾工作
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
