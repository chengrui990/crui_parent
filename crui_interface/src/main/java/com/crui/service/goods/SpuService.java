package com.crui.service.goods;
import com.crui.entity.PageResult;
import com.crui.pojo.goods.Goods;
import com.crui.pojo.goods.Spu;

import java.util.*;

/**
 * spu业务逻辑层
 */
public interface SpuService {


    public List<Spu> findAll();


    public PageResult<Spu> findPage(int page, int size);


    public List<Spu> findList(Map<String,Object> searchMap);


    public PageResult<Spu> findPage(Map<String,Object> searchMap,int page, int size);


    public Spu findById(String id);

    public void add(Spu spu);


    public void update(Spu spu);


    public void delete(String id);

    public void saveGoods(Goods goods);

    public Goods findGoodsById(String id);

    /**
     * 修改
     * @param id
     * @param status
     */
    public void audit(String id, String status,String message);

    public void pull(String id);

    public void put(String id);

    /**
     * 批量上架
     * @param ids
     * @return
     */
    public Integer putMany(String[] ids);

}
