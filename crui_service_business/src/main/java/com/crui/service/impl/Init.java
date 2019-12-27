package com.crui.service.impl;

import com.crui.service.business.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {
    @Autowired
    private AdService adService;
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("广告图缓存预热.....");
        adService.saveAllAd2Redis();
    }
}
