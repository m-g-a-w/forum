package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Orders;
import com.knowledge.backend.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/create")
    public Result<Orders> create(@RequestParam("columnId") Long columnId, @RequestAttribute("userId") Long userId) {
        try {
            Orders order = ordersService.createOrder(userId, columnId);
            if (order == null) {
                // 免费专栏，直接返回成功但没有订单流水
                return Result.success(null);
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam("orderNo") String orderNo, @RequestAttribute("userId") Long userId) {
        try {
            ordersService.payOrder(userId, orderNo);
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
