package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.crui.dao.SkuMapper;
import com.crui.dao.StockBackMapper;
import com.crui.pojo.goods.StockBack;
import com.crui.pojo.order.OrderItem;
import com.crui.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {
    @Autowired
    private StockBackMapper stockBackMapper;
    @Autowired
    private SkuMapper skuMapper;

    @Override
    @Transactional
    public void addList(List<OrderItem> orderItemList) {
        for (OrderItem orderItem:orderItemList){
            StockBack stockBack = new StockBack();
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setSkuId(orderItem.getSkuId());
            stockBack.setStatus("0");
            stockBack.setNum(orderItem.getNum());
            stockBack.setCreateTime(new Date());
            stockBackMapper.insert(stockBack);
        }
    }

    @Override
    @Transactional
    public void doBack() {
        System.out.println("StockBackServiceImpl,doBack,开始库存回滚..");
        //查询库存回滚表中状态为‘0’的记录
        Map<String, Object> searchmap = new HashMap<>();
        searchmap.put("status", "0");
        List<StockBack> stockBackList = stockBackMapper.selectByExample(createExample(searchmap));
        for (StockBack stockBack: stockBackList){
            skuMapper.deductionStock(stockBack.getSkuId(),-stockBack.getNum());
            skuMapper.addSaleNum(stockBack.getSkuId(),-stockBack.getNum());
            stockBack.setStatus("1");
            stockBack.setBackTime(new Date());
            stockBackMapper.updateByPrimaryKey(stockBack);
        }
        System.out.println("StockBackServiceImpl,doBack,库存回滚..结束");
    }





    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(StockBack.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 商品id
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }

        }
        return example;
    }
}
