package com.crui.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.service.order.CategoryReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OrderTask {
    @Reference
    private CategoryReportService categoryReportService;
//    @Scheduled(cron = "5/5 * * * * ?")
    public void orderTimeOutLogic(){
        System.out.println(new Date());
    }

    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void createCategoryReportData(){
        System.out.println("生成类目统计数据。。");
        categoryReportService.createData();
    }
}
