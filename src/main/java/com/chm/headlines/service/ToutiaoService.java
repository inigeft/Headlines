package com.chm.headlines.service;

import org.springframework.stereotype.Service;
//IoC
@Service            //这里的标记别忘了！
public class ToutiaoService {
    public String say() {
        return "This is from ToutiaoService";
    }
}
