package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.crui.dao.SeckillGoodsMapper;
import com.crui.service.seckill.SeckillOrderService;
import com.crui.task.MultiThreadingCreateOrder;
import com.crui.util.IdWorker;
import com.crui.util.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;
    @Override
    public Boolean add(String username, String id, String time) {
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username,1);
        if (userQueueCount>1){
            System.out.println("SeckillOrderServiceImpl->add, 重复抢单");
            throw new RuntimeException("100");
        }

        //创建队列所需的排队信息
        SeckillStatus seckillStatus = new SeckillStatus(username,new Date(),1,id,0,"",time);
        //将排队信息放到List中
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
//        异步操作调用
        multiThreadingCreateOrder.createOrder();
        System.out.println("SeckillOrderServiceImpl->add, 其他程序正在执行。。");
        return true;

    }

    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }
}
