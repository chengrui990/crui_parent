use crui_order;

select category_id1     categoryId1,
       category_id2     categoryId2,
       category_id3     categoryId3,
       date(a.pay_time) createDate,
       sum(b.num)       num,
       sum(b.pay_money) money
from tb_order a,
     tb_order_item b
where a.id = b.order_id
  and a.pay_status = '1'
  and a.is_delete = '0'
  and date_format(a.pay_time, '%Y-%m-%d') = '2019-04-15'
group by b.category_id1, b.category_id2, b.category_id3, date(a.pay_time);

select date_format(a.pay_time, '%Y-%m-%d') createDate
from tb_order a,
     tb_order_item b
where a.id = b.order_id
  and a.pay_status = '1'
  and a.is_delete = '0'
  and date_format(a.pay_time, '%Y-%m-%d') = '2019-04-15';

select category_id1                        categoryId1,
       category_id2                        categoryId2,
       category_id3                        categoryId3,
       date_format(a.pay_time, '%Y-%m-%d') countDate,
       sum(b.num)                          num,
       sum(b.pay_money)                    money
from tb_order a,
     tb_order_item b
where a.id = b.order_id
  and a.pay_status = '1'
  and a.is_delete = '0'
  and date_format(a.pay_time, '%Y-%m-%d') = ?
group by b.category_id1, b.category_id2, b.category_id3, date_format(a.pay_time, '%Y-%m-%d');

select category_id1 categoryId1,
       name         categoryName,
       sum(num)     num,
       sum(money)   money
from tb_category_report t,
     v_category1 c
where t.category_id1 = c.id
  and t.count_date >= '2019-04-15'
  and t.count_date <= '2019-04-15'
group by t.category_id1, c.name;

create view v_category1 as
select id, name from crui_goods.tb_category where parent_id=0;

select * from v_category1;
