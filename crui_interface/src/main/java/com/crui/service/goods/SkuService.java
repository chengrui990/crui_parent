package com.crui.service.goods;
import com.crui.entity.PageResult;
import com.crui.pojo.goods.Sku;
import com.crui.pojo.order.OrderItem;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {


    public List<Sku> findAll();


    public PageResult<Sku> findPage(int page, int size);


    public List<Sku> findList(Map<String,Object> searchMap);


    public PageResult<Sku> findPage(Map<String,Object> searchMap,int page, int size);


    public Sku findById(String id);

    public void add(Sku sku);


    public void update(Sku sku);


    public void delete(String id);

    public void saveAllPrice2Redis();

    public Integer findPrice(String id);

    public void savePrice2RedisById(String id,Integer price);

    public void deletePriceFromRedis(String id);

    boolean deductionStock(List<OrderItem> orderItemList);


}
