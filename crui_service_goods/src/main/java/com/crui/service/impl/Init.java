package com.crui.service.impl;

import com.crui.service.goods.CategoryService;
import com.crui.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热.....分类和价格");
        categoryService.saveCategoryTree2Redis();
        skuService.saveAllPrice2Redis();
    }
}
