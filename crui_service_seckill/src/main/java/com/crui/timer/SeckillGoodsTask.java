package com.crui.timer;

import com.crui.dao.SeckillGoodsMapper;
import com.crui.pojo.seckill.SeckillGoods;
import com.crui.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoods(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        //获取当前时间和之后的5个时间段
        List<Date> dateMenuList = DateUtil.getDateMenus();
        System.out.println(dateMenuList);
        for (Date startTime: dateMenuList){
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            criteria.andEqualTo("status", "1");
            criteria.andGreaterThan("stockCount",0);
            criteria.andGreaterThanOrEqualTo("startTime", startTime);
            criteria.andLessThan("endTime", DateUtil.addDateHour(startTime,2));


            //过滤Redis中已经存在的该区间的秒杀商品
            //测试时不过滤，因为要手动修改商品信息，
            //商品库存会变，应该存到缓存里面
            Set keys = redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(startTime)).keys();
            if (keys!=null && keys.size()>0){
                criteria.andNotIn("id", keys);
            }
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            System.out.println(simpleDateFormat.format(startTime)+",查到秒杀商品："+ (seckillGoodsList!=null? seckillGoodsList.size(): 0) +"个");
            for (SeckillGoods seckillGood : seckillGoodsList){
                //
                redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(startTime)).put(seckillGood.getId(), seckillGood);

                String[] ids = pushIds(seckillGood.getStockCount(), seckillGood.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(ids);
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(), seckillGood.getStockCount());
            }


        }
    }

    public String[] pushIds(int len, String id){
        String[] ids = new String[len];
        for (int i=0; i<len; i++){
            ids[i] = id;
        }
        return ids;
    }
}
