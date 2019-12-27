package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.crui.pojo.seckill.SeckillGoods;
import com.crui.service.seckill.SeckillGoodsService;
import com.crui.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<SeckillGoods> list(String time) {
        String key = "SeckillGoods_"+time;

        return redisTemplate.boundHashOps(key).values();
    }

    @Override
    public SeckillGoods one(String time, String id) {
        return (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+ time).get(id);
    }
}
