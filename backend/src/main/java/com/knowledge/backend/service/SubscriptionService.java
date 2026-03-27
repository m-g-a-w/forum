package com.knowledge.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.backend.entity.Subscription;

public interface SubscriptionService extends IService<Subscription> {
    boolean checkSubscribe(Long userId, Long columnId);
    Subscription getValidSubscription(Long userId, Long columnId);
}
