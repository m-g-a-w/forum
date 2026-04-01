package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.entity.Orders;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.OrdersService;
import com.knowledge.backend.service.SubscriptionService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam("columnId") Long columnId, @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.success(false);
        }
        ColumnInfo column = columnInfoService.getById(columnId);
        if (column != null && column.getCreatorId().equals(userId)) {
            return Result.success(true);
        }
        boolean isSubCheck = subscriptionService.checkSubscribe(userId, columnId);
        return Result.success(isSubCheck);
    }

    @GetMapping("/my")
    public Result<List<ColumnInfo>> mySubscriptions(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Subscription> subs = subscriptionService.list(
            new QueryWrapper<Subscription>().eq("user_id", userId).orderByDesc("create_time")
        );
        if (subs.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> columnIds = subs.stream().map(Subscription::getColumnId).collect(Collectors.toList());
        List<ColumnInfo> columns = columnInfoService.listByIds(columnIds);
        return Result.success(columns);
    }

    @GetMapping("/my-detail")
    public Result<List<Subscription>> mySubscriptionsDetail(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Subscription> subs = subscriptionService.list(
            new QueryWrapper<Subscription>().eq("user_id", userId).orderByDesc("create_time")
        );
        return Result.success(subs);
    }

    @PostMapping("/subscribe")
    @Transactional
    public Result<Subscription> subscribe(@RequestParam("columnId") Long columnId,
                                           @RequestParam("months") Integer months,
                                           @RequestAttribute("userId") Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (months == null || months < 1) {
            return Result.error(400, "订阅月数至少为1个月");
        }

        ColumnInfo column = columnInfoService.getById(columnId);
        if (column == null) {
            return Result.error(404, "专栏不存在");
        }
        if (column.getCreatorId().equals(userId)) {
            return Result.error(400, "不能订阅自己的专栏");
        }

        // 计算费用：月单价 * 月数
        BigDecimal monthlyPrice = column.getPrice();
        BigDecimal totalPrice = monthlyPrice.multiply(new BigDecimal(months));

        // 检查余额
        User user = userService.getById(userId);
        if (user.getBalance().compareTo(totalPrice) < 0) {
            return Result.error(400, "余额不足，请先充值");
        }

        // 扣除余额
        userService.updateById(user.setBalance(user.getBalance().subtract(totalPrice)));

        // 检查是否已有订阅，若有则续费，否则新建
        Subscription existingSub = subscriptionService.getOne(new QueryWrapper<Subscription>()
                .eq("user_id", userId)
                .eq("column_id", columnId));

        Subscription sub;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMonths(months);

        if (existingSub != null) {
            // 已有订阅，续费：从当前过期时间或现在起算
            LocalDateTime baseTime = existingSub.getExpireTime() != null && existingSub.getExpireTime().isAfter(now)
                ? existingSub.getExpireTime() : now;
            existingSub.setExpireTime(baseTime.plusMonths(months));
            existingSub.setDurationMonths((existingSub.getDurationMonths() != null ? existingSub.getDurationMonths() : 0) + months);
            subscriptionService.updateById(existingSub);
            sub = existingSub;
        } else {
            // 新订阅
            sub = new Subscription()
                    .setUserId(userId)
                    .setColumnId(columnId)
                    .setCreateTime(now)
                    .setExpireTime(expireTime)
                    .setDurationMonths(months);
            subscriptionService.save(sub);
        }

        // 创建订单记录
        Orders order = new Orders()
                .setOrderNo(UUID.randomUUID().toString().replace("-", ""))
                .setUserId(userId)
                .setColumnId(columnId)
                .setAmount(totalPrice)
                .setStatus(1) // 已支付
                .setCreateTime(now)
                .setPayTime(now);
        ordersService.save(order);

        return Result.success(sub);
    }

    @PostMapping("/renew")
    @Transactional
    public Result<Subscription> renew(@RequestParam("columnId") Long columnId,
                                      @RequestParam("months") Integer months,
                                      @RequestAttribute("userId") Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (months == null || months < 1) {
            return Result.error(400, "续费月数至少为1个月");
        }

        ColumnInfo column = columnInfoService.getById(columnId);
        if (column == null) {
            return Result.error(404, "专栏不存在");
        }

        Subscription existing = subscriptionService.getValidSubscription(userId, columnId);
        
        if (existing == null) {
            // 如果用户是专栏创建者但没有订阅记录，自动创建订阅记录（免费）
            if (column.getCreatorId().equals(userId)) {
                LocalDateTime now = LocalDateTime.now();
                Subscription newSub = new Subscription()
                        .setUserId(userId)
                        .setColumnId(columnId)
                        .setCreateTime(now)
                        .setExpireTime(now.plusMonths(months))
                        .setDurationMonths(months);
                subscriptionService.save(newSub);
                return Result.success(newSub);
            }
            return Result.error(400, "您还没有订阅该专栏，请先订阅");
        } else if (column.getCreatorId().equals(userId)) {
            // 创建者已经有订阅记录，续费也免费
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime baseTime = existing.getExpireTime().isAfter(now) ? existing.getExpireTime() : now;
            existing.setExpireTime(baseTime.plusMonths(months));
            existing.setDurationMonths(existing.getDurationMonths() + months);
            subscriptionService.updateById(existing);
            return Result.success(existing);
        }

        BigDecimal totalPrice = column.getPrice().multiply(new BigDecimal(months));

        User user = userService.getById(userId);
        if (user.getBalance().compareTo(totalPrice) < 0) {
            return Result.error(400, "余额不足，请先充值");
        }

        userService.updateById(user.setBalance(user.getBalance().subtract(totalPrice)));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseTime = existing.getExpireTime().isAfter(now) ? existing.getExpireTime() : now;
        existing.setExpireTime(baseTime.plusMonths(months));
        existing.setDurationMonths(existing.getDurationMonths() + months);
        subscriptionService.updateById(existing);

        Orders order = new Orders()
                .setOrderNo(UUID.randomUUID().toString().replace("-", ""))
                .setUserId(userId)
                .setColumnId(columnId)
                .setAmount(totalPrice)
                .setStatus(1)
                .setCreateTime(now)
                .setPayTime(now);
        ordersService.save(order);

        return Result.success(existing);
    }
}
