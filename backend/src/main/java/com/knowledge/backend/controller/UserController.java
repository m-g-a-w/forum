package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User userParam) {
        try {
            String token = userService.login(userParam.getUsername(), userParam.getPassword());
            User user = userService.lambdaQuery().eq(User::getUsername, userParam.getUsername()).one();
            user.setPassword(null);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User userParam) {
        try {
            User user = userService.register(userParam);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> info(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
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
        userService.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }
}
