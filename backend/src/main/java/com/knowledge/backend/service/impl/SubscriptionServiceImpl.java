package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.mapper.SubscriptionMapper;
import com.knowledge.backend.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    @Override
    public boolean checkSubscribe(Long userId, Long columnId) {
        if (userId == null || columnId == null) {
            return false;
        }
        Subscription sub = this.getOne(new QueryWrapper<Subscription>()
                .eq("user_id", userId)
                .eq("column_id", columnId));
        // 检查订阅是否存在且未过期
        if (sub != null && sub.getExpireTime() != null) {
            return sub.getExpireTime().isAfter(LocalDateTime.now());
        }
        return sub != null && sub.getExpireTime() == null; // 兼容旧数据
    }

    public Subscription getValidSubscription(Long userId, Long columnId) {
        if (userId == null || columnId == null) {
            return null;
        }
        Subscription sub = this.getOne(new QueryWrapper<Subscription>()
                .eq("user_id", userId)
                .eq("column_id", columnId));
        if (sub == null) {
            return null;
        }
        // 兼容旧数据：expireTime 为 null 表示永久订阅
        if (sub.getExpireTime() == null) {
            return sub;
        }
        // 检查订阅是否未过期
        if (sub.getExpireTime().isAfter(LocalDateTime.now())) {
            return sub;
        }
        return null;
    }
}
