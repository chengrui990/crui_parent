package com.crui.service.goods;
import com.crui.entity.PageResult;
import com.crui.pojo.goods.Pref;

import java.util.*;

/**
 * pref业务逻辑层
 */
public interface PrefService {


    public List<Pref> findAll();


    public PageResult<Pref> findPage(int page, int size);


    public List<Pref> findList(Map<String,Object> searchMap);


    public PageResult<Pref> findPage(Map<String,Object> searchMap,int page, int size);


    public Pref findById(Integer id);

    public void add(Pref pref);


    public void update(Pref pref);


    public void delete(Integer id);

}
