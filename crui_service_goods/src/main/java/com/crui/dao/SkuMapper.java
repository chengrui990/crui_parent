package com.crui.dao;

import com.crui.pojo.goods.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {

    @Select("update tb_sku set num=num-#{deNum} where id=#{id} ")
    public void deductionStock(@Param("id") String id, @Param("deNum") Integer num);

    @Select("update tb_sku set sale_num=sale_num+#{addNum} where id=#{id} ")
    public void addSaleNum(@Param("id") String id, @Param("addNum") Integer num);

}
