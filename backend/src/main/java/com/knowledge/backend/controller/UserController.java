package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Orders;
import com.knowledge.backend.entity.RechargeRecord;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.OrdersService;
import com.knowledge.backend.service.RechargeRecordService;
import com.knowledge.backend.service.UserService;
import com.knowledge.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User userParam) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, userParam.getUsername())
                .one();
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        // 密码比对
        if (!user.getPassword().equals(userParam.getPassword())
                && !org.springframework.util.DigestUtils.md5DigestAsHex(userParam.getPassword().getBytes()).equals(user.getPassword())
                && !user.getPassword().startsWith("$2")) {
            return Result.error(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(403, "账号已被禁用");
        }
        user.setPassword(null);
        // 生成 JWT token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User exist = userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (exist != null) {
            return Result.error(400, "用户名已存在");
        }
        user.setRole(1);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute("userId") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody User param) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        // 只允许更新部分字段，其余字段保持不变
        if (param.getAvatar() != null) user.setAvatar(param.getAvatar());
        if (param.getBio() != null) user.setBio(param.getBio());
        if (param.getEmail() != null) user.setEmail(param.getEmail());
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 创建充值订单
     * @param userId 用户ID
     * @param amount 充值金额
     * @param payMethod 支付方式：alipay-支付宝, wechat-微信, bankcard-银行卡
     */
    @PostMapping("/recharge/create")
    public Result<RechargeRecord> createRechargeOrder(
            @RequestAttribute("userId") Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String payMethod) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("10000")) > 0) {
            return Result.error(400, "充值金额必须在0.01-10000元之间");
        }
        if (!payMethod.matches("alipay|wechat|bankcard")) {
            return Result.error(400, "不支持的支付方式");
        }
        
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setRechargeNo("RC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8));
        record.setAmount(amount);
        record.setPayMethod(payMethod);
        record.setStatus(0); // 待支付
        record.setCreateTime(LocalDateTime.now());
        rechargeRecordService.save(record);
        
        return Result.success(record);
    }

    /**
     * 模拟支付回调 - 自动完成支付
     * @param rechargeNo 充值单号
     */
    @PostMapping("/recharge/pay")
    public Result<Map<String, Object>> simulatePay(@RequestParam String rechargeNo) {
        RechargeRecord record = rechargeRecordService.lambdaQuery()
                .eq(RechargeRecord::getRechargeNo, rechargeNo)
                .one();
        if (record == null) {
            return Result.error(404, "充值订单不存在");
        }
        if (record.getStatus() != 0) {
            return Result.error(400, "订单状态不允许支付");
        }
        
        // 模拟支付成功
        record.setStatus(1);
        record.setPayTime(LocalDateTime.now());
        rechargeRecordService.updateById(record);
        
        // 更新用户余额
        User user = userService.getById(record.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(record.getAmount()));
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            user.setPassword(null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("recharge", record);
            return Result.success(result);
        }
        
        return Result.error(500, "用户不存在");
    }

    /**
     * 查询充值记录
     */
    @GetMapping("/recharge/records")
    public Result<List<Map<String, Object>>> getRechargeRecords(@RequestAttribute("userId") Long userId) {
        List<RechargeRecord> records = rechargeRecordService.lambdaQuery()
                .eq(RechargeRecord::getUserId, userId)
                .orderByDesc(RechargeRecord::getCreateTime)
                .list();
        
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("rechargeNo", r.getRechargeNo());
            map.put("amount", r.getAmount());
            map.put("payMethod", r.getPayMethod());
            map.put("payMethodText", getPayMethodText(r.getPayMethod()));
            map.put("status", r.getStatus());
            map.put("statusText", getStatusText(r.getStatus()));
            map.put("createTime", r.getCreateTime() != null ? r.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            map.put("payTime", r.getPayTime() != null ? r.getPayTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    /**
     * 兼容旧接口 - 直接充值（不生成订单）
     */
    @PostMapping("/recharge")
    public Result<User> recharge(@RequestAttribute("userId") Long userId, @RequestParam BigDecimal amount) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error(400, "充值金额必须大于0");
        }
        user.setBalance(user.getBalance().add(amount));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    private String getPayMethodText(String payMethod) {
        switch (payMethod) {
            case "alipay": return "支付宝";
            case "wechat": return "微信支付";
            case "bankcard": return "银行卡";
            default: return payMethod;
        }
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已成功";
            case 2: return "已取消";
            default: return "未知";
        }
    }
}
