package com.crui.service.order;

import java.util.List;
import java.util.Map;

public interface CartService {

    List<Map<String, Object>> findCartList(String username);

    void updateItem(String username, String skuId, Integer num);

    boolean updateChecked(String usernme, String skuId, boolean checked);

    void deleteCheckedCart(String username);

    int preferential(String username);

    List<Map<String, Object>> findNewOrderItemList(String username);
}
