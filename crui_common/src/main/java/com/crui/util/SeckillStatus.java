package com.crui.util;

import java.io.Serializable;
import java.util.Date;

public class SeckillStatus implements Serializable {
    private String username;

    private Date createTime;
    private Integer status;
    private String goodsId;
    private Integer money;
    private String orderId;
    private String time;

    public SeckillStatus(String username, Date createTime, Integer status, String goodsId, Integer money, String orderId, String time) {
        this.username = username;
        this.createTime = createTime;
        this.status = status;
        this.goodsId = goodsId;
        this.money = money;
        this.orderId = orderId;
        this.time = time;
    }

    public SeckillStatus() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
