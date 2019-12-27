package com.crui.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.crui.entity.Result;
import com.crui.service.seckill.SeckillOrderService;
import com.crui.util.SeckillStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {
    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/add")
    public Result add(String id,String time){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("SeckillOrderController,登录用户："+username);
        if (username.equals("anonymousUser")){
            return new Result(403,"请先登录");
        }
        try {
            Boolean isAdded = seckillOrderService.add(username,id,time);
            if (isAdded){
                return new Result();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Result(2,e.getMessage());
        }

        return new Result(1, "抢单失败！");
    }
    @RequestMapping("/status")
    public Result queryStatus(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)){
            return new Result(403, "用户未登录");
        }
        try{
            SeckillStatus seckillStatus = seckillOrderService.queryStatus(username);
            if (seckillStatus!=null){
                return new Result(seckillStatus.getStatus(),"抢单状态");
            }
        }catch (Exception  e){
            e.printStackTrace();
            return new Result(0,e.getMessage());//抢单失败
        }

        return new Result(404,"无相关信息");
    }

}
