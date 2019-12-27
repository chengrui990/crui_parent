package com.crui.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.service.goods.StockBackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SkuTask {
    @Reference
    private StockBackService stockBackService;

    /**
     * 间隔一小时执行一次库存回滚
     */
//    @Scheduled(cron = "0 0 0/1 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void skuStockBack(){
        System.out.println("SkuTask,商品库存回滚开始。。");
        stockBackService.doBack();
        System.out.println("SkuTask,商品库存回滚结束。。");
    }
}
