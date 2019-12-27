package com.crui.service.order;
import com.crui.entity.PageResult;
import com.crui.pojo.order.Order;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public Map<String, Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

    void updatePayStatus(String orderId, String transactionId);

}
