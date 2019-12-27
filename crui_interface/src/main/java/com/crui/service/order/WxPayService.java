package com.crui.service.order;

import java.util.Map;

public interface WxPayService {

    public Map createNative(String orderId, Integer money, String notifyUrl);

    void notifyLogic(String xml);
}
