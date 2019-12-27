package com.crui.service.seckill;

import com.crui.util.SeckillStatus;

public interface SeckillOrderService {
    Boolean add(String username, String id, String time);

    SeckillStatus queryStatus(String username);
}
