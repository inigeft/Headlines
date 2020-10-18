package com.chm.headlines.async.handler;

import com.chm.headlines.async.EventHandler;
import com.chm.headlines.async.EventModel;
import com.chm.headlines.async.EventType;
import com.chm.headlines.model.Message;
import com.chm.headlines.model.User;
import com.chm.headlines.service.MessageService;
import com.chm.headlines.service.NewsService;
import com.chm.headlines.service.UserService;
import com.chm.headlines.util.ToutiaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    NewsService newsService;

    @Override
    public void doHandler(EventModel eventModel) {
        Message message = new Message();
        User user = userService.getUser(eventModel.getActorId());

        message.setContent("用户" + user.getName() + "赞了你的资讯 "
                            + ToutiaoUtil.TOUTIAO_DOMAIN +"news/"+String.valueOf(eventModel.getEntityId()));
        // 默认 id=1 的用户为系统管理员
        int fromId = 1;
        int toId = eventModel.getEntityOwnerId();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setCreatedDate(new Date());
        message.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);           // 不明白这里 Arrays.asList(EventType.LIKE) 的意思
    }
}
