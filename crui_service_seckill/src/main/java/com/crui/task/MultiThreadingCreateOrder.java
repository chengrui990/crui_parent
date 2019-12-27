package com.crui.task;

import com.crui.dao.SeckillGoodsMapper;
import com.crui.pojo.seckill.SeckillGoods;
import com.crui.pojo.seckill.SeckillOrder;
import com.crui.util.IdWorker;
import com.crui.util.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;


    /**
     * 异步操作方法
     * 注解 @Async
     */
    @Async
    public void createOrder(){
        try {
            System.out.println("MultiThreadingCreateOrder,createOrder，---准备@Async执行");
            Thread.sleep(10000);

            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();


            String username = seckillStatus.getUsername();
            String time = seckillStatus.getTime();
            String id = seckillStatus.getGoodsId();
            Object sid = redisTemplate.boundListOps("SeckillGoodsCountList_"+id).rightPop();
            if (sid==null){//買完了
                clearQueue(seckillStatus);
                return;
            }
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+time).get(id);
            if (seckillGoods!=null && seckillGoods.getStockCount()>0){
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId()+"");
                seckillOrder.setSeckillId(id);
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                seckillOrder.setUserId(username);
                seckillOrder.setSellerId(seckillGoods.getSellerId());
//            seckillOrder.setCreateTime();
                seckillOrder.setStatus("0");

                redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

                //削减库存,当库存不足时，同步到MySQL并清理Redis缓存，库存充足时，不需要更新到MySQL
                Long seckillGoodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(),-1);

                seckillGoods.setStockCount(seckillGoodsCount.intValue());
                if (seckillGoodsCount<=0){
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    redisTemplate.boundHashOps("SeckillGoods_"+time).delete(id);
                }else {
                    redisTemplate.boundHashOps("SeckillGoods_"+time).put(id,seckillGoods);
                }

                seckillStatus.setStatus(2);
                seckillStatus.setMoney(seckillOrder.getMoney());
                seckillStatus.setOrderId(seckillOrder.getId());
                redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
            }else {
//                return false;
            }
//            return true;
            System.out.println("MultiThreadingCreateOrder,createOrder，---正在执行---");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void clearQueue(SeckillStatus seckillStatus){
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());
    }
}
