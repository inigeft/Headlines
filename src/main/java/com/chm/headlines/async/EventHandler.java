package com.chm.headlines.async;

import java.util.List;

public interface EventHandler {
    void doHandler(EventModel model);

    List<EventType> getSupportEventTypes();     //定义需要处理的EvenType的事件
}
