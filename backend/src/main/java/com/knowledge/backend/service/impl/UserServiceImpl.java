package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.mapper.UserMapper;
import com.knowledge.backend.service.UserService;
import com.knowledge.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 我们还是可以用BCrypt，单独引入或者自己写简易的
// 为了KISS原则，我们在pom里引入了spring-boot-starter-security，所以可以直接用 BCryptPasswordEncoder
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        User user = this.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        return jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());
    }

    @Override
    public User register(User user) {
        User exist = this.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setBalance(BigDecimal.valueOf(100));
        user.setStatus(1);
        if (user.getRole() == null) {
            user.setRole(1); // 默认创作中心角色，全民创作
        }
        this.save(user);
        user.setPassword(null); // 清除返回体中的密码
        return user;
    }
}
