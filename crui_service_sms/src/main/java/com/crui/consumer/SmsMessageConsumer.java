package com.crui.consumer;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public class SmsMessageConsumer implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;
    @Value("${smsCode}")
    private String smsCode;
    @Value("${param}")
    private String param;

    /**
     *  当queue.sms队列中有消息到达时，取出message中的消息内容，并调用阿里云通信发送验证码短信
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        String jsonString = new String(message.getBody());
        Map<String,String> map = JSON.parseObject(jsonString, Map.class);

        String phone = map.get("phone");
        String code = map.get("code");
        System.out.println("SmsMessageConsumer,从RabbitMQ接收, 手机号："+phone+ ",验证码："+ code);

        //调用阿里云通信发送短信到手机
        smsUtil.sendSms(phone,smsCode,param.replace("[value]", code));
    }
}
