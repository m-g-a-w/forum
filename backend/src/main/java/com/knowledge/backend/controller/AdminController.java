package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.OrdersService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private OrdersService ordersService;

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
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(403, e.getMessage());
        }
    }
}
