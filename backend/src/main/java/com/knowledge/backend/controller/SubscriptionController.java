package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private ColumnInfoService columnInfoService;

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam("columnId") Long columnId, @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.success(false);
        }
        // 创作者本人可以直接看
        ColumnInfo column = columnInfoService.getById(columnId);
        if (column != null && column.getCreatorId().equals(userId)) {
            return Result.success(true);
        }
        boolean isSubCheck = subscriptionService.checkSubscribe(userId, columnId);
        return Result.success(isSubCheck);
    }

    @GetMapping("/my")
    public Result<List<ColumnInfo>> mySubscriptions(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<Subscription> subs = subscriptionService.list(
            new QueryWrapper<Subscription>().eq("user_id", userId).orderByDesc("create_time")
        );
        if (subs.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> columnIds = subs.stream().map(Subscription::getColumnId).collect(Collectors.toList());
        List<ColumnInfo> columns = columnInfoService.listByIds(columnIds);
        return Result.success(columns);
    }
}
