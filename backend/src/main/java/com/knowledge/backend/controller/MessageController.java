package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Message;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.MessageService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Message>> list() {
        List<Message> list = messageService.lambdaQuery().orderByDesc(Message::getCreateTime).list();
        return Result.success(list);
    }

    @PostMapping("/post")
    public Result<Void> post(
            @RequestBody Message message,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        
        if (userId != null) {
            User user = userService.getById(userId);
            message.setUserId(userId);
            message.setNickname(user.getUsername());
            message.setAvatar(user.getAvatar());
        } else {
            if (message.getNickname() == null) message.setNickname("匿名岛民");
        }
        message.setCreateTime(LocalDateTime.now());
        messageService.save(message);
        return Result.success(null);
    }
}
