package com.knowledge.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.backend.entity.Orders;

public interface OrdersService extends IService<Orders> {
    Orders createOrder(Long userId, Long columnId);
    boolean payOrder(Long userId, String orderNo);
}
