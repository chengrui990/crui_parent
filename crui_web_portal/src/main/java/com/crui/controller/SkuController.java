package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.entity.Result;
import com.crui.service.goods.SkuSearchService;
import com.crui.service.goods.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sku")
@CrossOrigin
public class SkuController {
    @Reference
    private SkuService skuService;
    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/price")
    public Integer price(String id){
        return skuService.findPrice(id);
    }
    @GetMapping("/saveAllSku")
    public Result saveAll2Elastic(){
        skuSearchService.saveAll2Elastic();
        return new Result();
    }

}
