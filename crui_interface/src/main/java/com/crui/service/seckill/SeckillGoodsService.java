package com.crui.service.seckill;

import com.crui.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    List<SeckillGoods> list(String time);
    SeckillGoods one(String time, String id);
}
