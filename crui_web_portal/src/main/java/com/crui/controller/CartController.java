package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.entity.Result;
import com.crui.pojo.order.Order;
import com.crui.pojo.user.Address;
import com.crui.service.order.CartService;
import com.crui.service.order.OrderService;
import com.crui.service.user.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Reference
    private AddressService addressService;

    @Reference
    private OrderService orderService;

    @GetMapping("/findCartList")
    public List<Map<String, Object>> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return cartService.findCartList(username);
    }

    @GetMapping("/addItem")
    public Result addItem(String skuId, Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateItem(username,skuId,num);
        return new Result();
    }

    @GetMapping("/buy")
    public void buy(HttpServletResponse response, String skuId, Integer num) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateItem(username,skuId,num);
        response.sendRedirect("/cart.html");
    }
    @GetMapping("/updateChecked")
    public Result updateChecked(String skuId, boolean checked) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateChecked(username,skuId,checked);
        return new Result();
    }

    @GetMapping("deleteCheckedCart")
    public Result deleteCheckedCart(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.deleteCheckedCart(username);
        return new Result();
    }

    @GetMapping("/preferential")
    public Map preferential(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int allPreMoney = cartService.preferential(username);
        Map map = new HashMap();
        map.put("allPreMoney",allPreMoney);
        return map;
    }

    @GetMapping("/findNewOrderItemList")
    public List<Map<String, Object>> findNewOrderItemList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return cartService.findNewOrderItemList(username);
    }

    @GetMapping("/findAddressList")
    public List<Address> findAddressList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findByUsername(username);
    }
    @PostMapping("/saveOrder")
    public Map<String, Object> saveOrder(@RequestBody Order order){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUsername(username);
        return orderService.add(order);
    }
}
