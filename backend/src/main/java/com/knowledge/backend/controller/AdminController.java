package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private SubscriptionService subscriptionService;

    // 前置权限校验：所有接口都需要 role == 2
    private void checkAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new RuntimeException("无权限，仅管理员可访问");
        }
    }

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> listUsers(@RequestAttribute("role") Integer role) {
        try {
            checkAdmin(role);
            List<User> users = userService.list();
            List<Map<String, Object>> result = users.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                map.put("status", user.getStatus());
                // 统计该用户的专栏数量
                long columnCount = columnInfoService.lambdaQuery()
                    .eq(ColumnInfo::getCreatorId, user.getId()).count();
                map.put("columnCount", columnCount);
                return map;
            }).collect(java.util.stream.Collectors.toList());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PostMapping("/user/status")
    public Result<String> changeUserStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status, @RequestAttribute("role") Integer role) {
        try {
            checkAdmin(role);
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            userService.updateById(user);
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }

    @GetMapping("/columns")
    public Result<List<ColumnInfo>> listColumns(@RequestAttribute("role") Integer role) {
        try {
            checkAdmin(role);
            return Result.success(columnInfoService.list());
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PostMapping("/column/status")
    public Result<String> changeColumnStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status, @RequestAttribute("role") Integer role) {
        try {
            checkAdmin(role);
            ColumnInfo column = new ColumnInfo();
            column.setId(id);
            column.setStatus(status);
            columnInfoService.updateById(column);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }
    
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestAttribute("role") Integer role) {
        try {
            checkAdmin(role);
            Map<String, Object> data = new HashMap<>();
            data.put("userCount", userService.count());
            data.put("columnCount", columnInfoService.count());
            data.put("orderCount", ordersService.count());
            data.put("subscriptionCount", subscriptionService.count());

            // 近期活跃用户（按更新时间倒序取前10个）
            List<User> recentUsers = userService.lambdaQuery()
                .orderByDesc(User::getUpdateTime)
                .last("LIMIT 10")
                .list();
            data.put("recentUsers", recentUsers.stream().map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("username", u.getUsername());
                map.put("email", u.getEmail());
                map.put("createTime", u.getCreateTime() != null ? u.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
                return map;
            }).collect(Collectors.toList()));

            // 最新订单（按创建时间倒序，取前20条）
            List<Orders> recentOrders = ordersService.lambdaQuery()
                .orderByDesc(Orders::getCreateTime)
                .last("LIMIT 20")
                .list();
            // 获取所有用户昵称
            Map<Long, String> userNameMap = userService.list().stream()
                .collect(Collectors.toMap(User::getId, u -> u.getUsername()));
            data.put("recentOrders", recentOrders.stream().map(o -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", o.getId());
                map.put("userId", o.getUserId());
                map.put("username", userNameMap.getOrDefault(o.getUserId(), "未知用户"));
                map.put("columnId", o.getColumnId());
                map.put("amount", o.getAmount());
                map.put("status", o.getStatus());
                map.put("createTime", o.getCreateTime() != null ? o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                return map;
            }).collect(Collectors.toList()));

            // 活跃用户（有订阅或订单的去重用户数）
            long activeUserCount = ordersService.lambdaQuery()
                .select(Orders::getUserId)
                .list()
                .stream()
                .map(Orders::getUserId)
                .filter(id -> id != null)
                .distinct()
                .count();
            data.put("activeUserCount", activeUserCount);

            // 今日新注册
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long todayRegister = userService.lambdaQuery()
                .ge(User::getCreateTime, todayStart)
                .count();
            data.put("todayRegister", todayRegister);

            // 今日订单
            long todayOrder = ordersService.lambdaQuery()
                .ge(Orders::getCreateTime, todayStart)
                .count();
            data.put("todayOrder", todayOrder);

            // 总收入
            BigDecimal totalRevenue = ordersService.lambdaQuery()
                .eq(Orders::getStatus, 1)
                .select(Orders::getAmount)
                .list()
                .stream()
                .map(Orders::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("totalRevenue", totalRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }
}
