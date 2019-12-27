package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.crui.pojo.goods.Category;
import com.crui.pojo.goods.Sku;
import com.crui.pojo.order.OrderItem;
import com.crui.service.goods.CategoryService;
import com.crui.service.goods.SkuService;
import com.crui.service.order.CartService;
import com.crui.service.order.PreferentialService;
import com.crui.util.CacheKey;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Reference
    private SkuService skuService;
    @Reference
    private CategoryService categoryService;
    @Autowired
    private PreferentialService preferentialService;

    /**
     * 从Redis缓存中读取购物车
     * @param username
     * @return
     */
    @Override
    public List<Map<String, Object>> findCartList(String username) {
        System.out.println("CartServiceImpl, 从redis提取购物车.."+username);
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        System.out.println(cartList);
        if (cartList==null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void updateItem(String username, String skuId, Integer num) {
        //遍历购物车，如果存在该商品就累加数量，如果不存在就添加到购物车项
        List<Map<String, Object>> cartList = findCartList(username);
        boolean isInCart = false;
        for (Map map: cartList){
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)){    //购物车中存在该商品
                if (orderItem.getNum()<=0){
                    cartList.remove(map);
                    isInCart = true;
                    break;
                }
                int weight = orderItem.getWeight()/orderItem.getNum();
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setMoney(orderItem.getPrice()*orderItem.getNum());
                orderItem.setWeight(weight*orderItem.getNum());

                if (orderItem.getNum()<=0){
                    cartList.remove(map);
                }
                isInCart = true;
                break;
            }
        }
        //购物车中没有该商品
        if (!isInCart){
            if (num<=0){
                throw new RuntimeException("商品数量不合法");
            }
            Sku sku = skuService.findById(skuId);
            if (sku==null){
                throw new RuntimeException("商品不存在");
            }
            if (!"1".equals(sku.getStatus())){
                throw new RuntimeException("商品状态不正确");
            }
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(sku,orderItem);
            orderItem.setSkuId(skuId);
            orderItem.setNum(num);
            orderItem.setMoney(orderItem.getPrice()*num);
            if (sku.getWeight()==null){
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight()*num);
            //商品分类
            orderItem.setCategoryId3(sku.getCategoryId());
            Category category3 = (Category)redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if (category3==null){
                category3 = categoryService.findById(sku.getCategoryId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(),category3);
            }
            orderItem.setCategoryId2(category3.getParentId());

            Category category2 = (Category)redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if (category2==null){
                category2 = categoryService.findById(category3.getParentId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category3.getParentId(),category2);
            }
            orderItem.setCategoryId1(category2.getParentId());

            Map map = new HashMap();
            map.put("item",orderItem);
            map.put("checked",true);
            cartList.add(map);
        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
    }

    @Override
    public boolean updateChecked(String usernme, String skuId, boolean checked) {
        List<Map<String, Object>> cartList = findCartList(usernme);
        boolean isOK = false;
        for (Map map: cartList){
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)) {
                map.put("checked",checked);
                isOK = true;
                break;
            }
        }
        if (isOK){
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(usernme,cartList);
        }
        return isOK;
    }

    @Override
    public void deleteCheckedCart(String username) {
        List<Map<String, Object>> unCheckedCartList = findCartList(username).stream().filter(cart -> (boolean)cart.get("checked")==false).collect(Collectors.toList());
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,unCheckedCartList);
    }

    @Override
    public int preferential(String username) {
        //获取选中的购物车
        List<OrderItem> orderItemList = findCartList(username).stream()
                .filter(cart -> (boolean)cart.get("checked")==true)
                .map(cart -> (OrderItem)cart.get("item"))
                .collect(Collectors.toList());
        //按category分类统计金额
        Map<Integer, IntSummaryStatistics> cartMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getCategoryId3, Collectors.summarizingInt(OrderItem::getMoney)));
        int allPreMoney = 0;
        for (Integer categoryId: cartMap.keySet()){
            int money = (int)cartMap.get(categoryId).getSum();
            System.out.println("money="+money);
            int preMoney =  preferentialService.findPreMoneyByCategoryId(categoryId,money);
            System.out.println("preMoney="+preMoney);
            allPreMoney+=preMoney;
            System.out.println("CartServiceImpl,preferential,分类："+categoryId+", 消费金额："+ money+ ", 优惠金额："+ allPreMoney);
        }
        //累加优惠金额
        return allPreMoney;
    }

    @Override
    public List<Map<String, Object>> findNewOrderItemList(String username) {
        List<Map<String, Object>> cartList = findCartList(username);
        for (Map<String, Object> map: cartList){
            OrderItem orderItem = (OrderItem) map.get("item");
            Sku sku = skuService.findById(orderItem.getSkuId());
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice()*orderItem.getNum());

        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        return cartList;
    }
}
