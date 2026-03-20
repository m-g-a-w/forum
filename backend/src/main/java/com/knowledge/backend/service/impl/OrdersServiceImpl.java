package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.entity.Orders;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.mapper.OrdersMapper;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.OrdersService;
import com.knowledge.backend.service.SubscriptionService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(Long userId, Long columnId) {
        if (subscriptionService.checkSubscribe(userId, columnId)) {
            throw new RuntimeException("您已订阅该专栏，无需重复购买");
        }
        ColumnInfo columnInfo = columnInfoService.getById(columnId);
        if (columnInfo == null || columnInfo.getStatus() == 0) {
            throw new RuntimeException("专栏不存在或已下架");
        }
        // 如果价格为0，直接变订阅
        if (columnInfo.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            Subscription sub = new Subscription().setUserId(userId).setColumnId(columnId);
            subscriptionService.save(sub);
            return null; // 表示免费，直接订阅成功
        }
        
        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setColumnId(columnId);
        order.setAmount(columnInfo.getPrice());
        order.setStatus(0); // 待支付
        this.save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long userId, String orderNo) {
        Orders order = this.getOne(new QueryWrapper<Orders>().eq("order_no", orderNo));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("越权操作");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常");
        }

        User user = userService.getById(userId);
        if (user.getBalance().compareTo(order.getAmount()) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }

        // 扣款
        user.setBalance(user.getBalance().subtract(order.getAmount()));
        userService.updateById(user);

        // 改订单状态
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);

        // 写入订阅表
        Subscription sub = new Subscription().setUserId(userId).setColumnId(order.getColumnId());
        subscriptionService.save(sub);

        // 分成给创作者(简化版：全额给创作者)
        ColumnInfo columnInfo = columnInfoService.getById(order.getColumnId());
        User creator = userService.getById(columnInfo.getCreatorId());
        creator.setBalance(creator.getBalance().add(order.getAmount()));
        userService.updateById(creator);

        return true;
    }
}
