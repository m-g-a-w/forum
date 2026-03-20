package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.mapper.SubscriptionMapper;
import com.knowledge.backend.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    @Override
    public boolean checkSubscribe(Long userId, Long columnId) {
        if (userId == null || columnId == null) {
            return false;
        }
        long count = this.count(new QueryWrapper<Subscription>()
                .eq("user_id", userId)
                .eq("column_id", columnId));
        return count > 0;
    }
}
