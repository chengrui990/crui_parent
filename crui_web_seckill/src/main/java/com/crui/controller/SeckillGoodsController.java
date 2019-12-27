package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.pojo.seckill.SeckillGoods;
import com.crui.service.seckill.SeckillGoodsService;
import com.crui.util.DateUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/seckill/goods")
public class SeckillGoodsController {
    @Reference
    private SeckillGoodsService seckillGoodsService;

    @GetMapping("/loadMenus")
    public List<Date> loadMenus(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("SeckillGoodsController登录用户："+username);
        return DateUtil.getDateMenus();
    }

    /**
     *
     * @param time : 2019010101
     * @return
     */
    @GetMapping("/list")
    public List<SeckillGoods> list(String time){
        System.out.println("SeckillGoodsController");
        return seckillGoodsService.list(time);
    }

    @GetMapping("/one")
    public SeckillGoods one(String time,String id){
        return seckillGoodsService.one(time,id);
    }

}
