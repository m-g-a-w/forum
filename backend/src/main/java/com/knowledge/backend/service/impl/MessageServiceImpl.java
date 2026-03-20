package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.Message;
import com.knowledge.backend.mapper.MessageMapper;
import com.knowledge.backend.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
}
