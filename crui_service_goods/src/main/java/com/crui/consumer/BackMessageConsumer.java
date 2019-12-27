package com.crui.consumer;

import com.alibaba.fastjson.JSON;
import com.crui.pojo.order.OrderItem;
import com.crui.service.goods.StockBackService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BackMessageConsumer implements MessageListener {
    @Autowired
    private StockBackService stockBackService;
    @Override
    public void onMessage(Message message) {
        //提取消息
        try {
            System.out.println("BackMessageConsumer,处理库存消息..");
            String jsonString  = new String(message.getBody());
            List<OrderItem> orderItemList = JSON.parseArray(jsonString, OrderItem.class);
            stockBackService.addList(orderItemList);
        }catch (Exception e){
            e.printStackTrace();
            //记录日志，之后人工干预
        }

    }
}
