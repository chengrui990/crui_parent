package com.crui.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.crui.service.order.OrderService;
import com.crui.service.order.WxPayService;
import com.github.wxpay.sdk.Config;
import com.github.wxpay.sdk.WXPayRequest;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private Config config;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public Map createNative(String orderId, Integer money, String notifyUrl) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", config.getAppID());
        map.put("mch_id", config.getMchID());
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("body", "买崽子");
        map.put("out_trade_no", orderId);
        map.put("total_fee", money.toString());
        map.put("spbill_create_ip","");
        map.put("notify_url",notifyUrl);
        map.put("trade_type","NATIVE");
        try {
            String xmlParam = WXPayUtil.generateSignedXml(map, config.getKey());
            System.out.println("WxPayServiceImpl,createNative,参数："+xmlParam);

            WXPayRequest wxPayRequest = new WXPayRequest(config);
            String xmlResult = wxPayRequest.requestWithCert("/pay/unifiedorder",null,xmlParam,false);
            System.out.println("结果："+ xmlResult);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);

            Map m = new HashMap();
            m.put("code_url", resultMap.get("code_url"));
            m.put("total_fee", money+"");
            m.put("out_trade_no", orderId);
            return m;
        }catch (Exception e){
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public void notifyLogic(String xml) {
        try {
            //解析xml为map
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            //验证签名
            boolean signnatureValid = WXPayUtil.isSignatureValid(map, config.getKey());
            System.out.println("签名验证通过?"+signnatureValid);
            //修改订单状态
            if (signnatureValid){
                if ("SUCCESS".equals(map.get("result_code"))){
                    orderService.updatePayStatus(map.get("out_trade_no"), map.get("transaction_id"));
                    //发送订单号给MQ
                    rabbitTemplate.convertAndSend("paynotify","", map.get("out_trade_no"));
                }

            }else {

            }


        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
