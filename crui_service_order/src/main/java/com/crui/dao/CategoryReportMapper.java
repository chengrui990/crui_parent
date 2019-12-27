package com.crui.dao;

import com.crui.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportMapper extends Mapper<CategoryReport> {
    @Select("select category_id1 categoryId1, " +
            "       category_id2 categoryId2, " +
            "       category_id3 categoryId3, " +
            "       date_format(a.pay_time, '%Y-%m-%d') countDate, " +
            "       sum(b.num)                          num, " +
            "       sum(b.pay_money)                    money " +
            "from tb_order a, " +
            "     tb_order_item b " +
            "where a.id = b.order_id " +
            "  and a.pay_status = '1' " +
            "  and a.is_delete = '0' " +
            "  and date_format(a.pay_time, '%Y-%m-%d') = #{date} " +
            "group by b.category_id1, b.category_id2, b.category_id3, date_format(a.pay_time, '%Y-%m-%d')")
    public List<CategoryReport> categoryReport(@Param("date")LocalDate date);

    @Select("select category_id1 categoryId1, " +
            "       name         categoryName, " +
            "       sum(num)     num, " +
            "       sum(money)   money " +
            "from tb_category_report t, " +
            "     v_category1 c " +
            "where t.category_id1 = c.id " +
            "  and t.count_date >= #{startDate} " +
            "  and t.count_date <= #{endDate} " +
            "group by t.category_id1, c.name")
    public List<Map> category1Count(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
