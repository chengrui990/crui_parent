package com.crui.service.goods;

import com.crui.pojo.order.OrderItem;

import java.util.List;

public interface StockBackService {
    public void addList(List<OrderItem> orderItemList);

    /**
     * 执行库存回滚
     */
    void doBack();
}
