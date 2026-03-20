package com.knowledge.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.backend.entity.User;

public interface UserService extends IService<User> {
    String login(String username, String password);
    User register(User user);
}
