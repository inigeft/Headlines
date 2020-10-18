package com.chm.headlines.async.handler;

import com.chm.headlines.async.EventHandler;
import com.chm.headlines.async.EventModel;
import com.chm.headlines.async.EventType;
import com.chm.headlines.model.Message;
import com.chm.headlines.service.MessageService;
import com.chm.headlines.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LoginExceptionHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Autowired
    MailSender mailSender;

    @Override
    public void doHandler(EventModel eventModel) {
        Message message = new Message();
        //这里假设id=1的用户为管理员账号
        int fromId = 1;
        int toId = eventModel.getActorId();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
        message.setContent("欢迎回来！");
        message.setCreatedDate(new Date());
        messageService.addMessage(message);

        Map<String, Object> map = new HashMap();
        map.put("username", eventModel.getExt("username"));
        mailSender.sendWithHTMLTemplate(eventModel.getExt("to"), "欢迎回来",
                "mails/loginWelcome.html", map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
