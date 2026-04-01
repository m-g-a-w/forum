package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Article;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.entity.Subscription;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.ArticleService;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.SubscriptionService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/column")
public class ColumnController {

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/create")
    public Result<ColumnInfo> create(@RequestBody ColumnInfo columnInfo, @RequestAttribute("userId") Long userId) {
        columnInfo.setCreatorId(userId);
        columnInfo.setStatus(1); // 默认上架
        columnInfoService.save(columnInfo);

        // 自动将用户角色改为创作者
        User user = userService.getById(userId);
        if (user != null && user.getRole() == 0) {
            user.setRole(1);
            userService.updateById(user);
        }

        return Result.success(columnInfo);
    }

    @GetMapping("/list/public")
    public Result<List<Map<String, Object>>> listPublic() {
        List<ColumnInfo> list = columnInfoService.lambdaQuery()
                .eq(ColumnInfo::getStatus, 1)
                .orderByDesc(ColumnInfo::getCreateTime)
                .list();

        // 转换为包含热度的 Map
        List<Map<String, Object>> result = list.stream().map(column -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", column.getId());
            map.put("creatorId", column.getCreatorId());
            map.put("title", column.getTitle());
            map.put("description", column.getDescription());
            map.put("cover", column.getCover());
            map.put("price", column.getPrice());
            map.put("status", column.getStatus());
            map.put("createTime", column.getCreateTime());
            map.put("updateTime", column.getUpdateTime());
            map.put("articleCount", column.getArticleCount());
            // 计算热度：订阅数量 + 续费次数
            Long subscriptionCount = subscriptionService.lambdaQuery()
                    .eq(Subscription::getColumnId, column.getId())
                    .count();
            // 获取该专栏的总订阅月数（续费累加）
            List<Subscription> subs = subscriptionService.lambdaQuery()
                    .eq(Subscription::getColumnId, column.getId())
                    .list();
            int totalMonths = subs.stream()
                    .mapToInt(s -> s.getDurationMonths() != null ? s.getDurationMonths() : 0)
                    .sum();
            // 热度 = 订阅人数 * 10 + 总订阅月数
            map.put("heat", subscriptionCount * 10 + totalMonths);
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<List<ColumnInfo>> myColumns(@RequestAttribute("userId") Long userId) {
        List<ColumnInfo> list = columnInfoService.lambdaQuery()
                .eq(ColumnInfo::getCreatorId, userId)
                .orderByDesc(ColumnInfo::getCreateTime)
                .list();
        
        // 为每个专栏填充文章数量
        for (ColumnInfo col : list) {
            Long count = articleService.lambdaQuery()
                    .eq(Article::getColumnId, col.getId())
                    .count();
            col.setArticleCount(count.intValue());
        }
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    public Result<ColumnInfo> detail(@PathVariable("id") Long id) {
        ColumnInfo columnInfo = columnInfoService.getById(id);
        if (columnInfo == null || columnInfo.getStatus() == 0) {
            return Result.error(404, "专栏不存在或已下架");
        }
        return Result.success(columnInfo);
    }

    @PostMapping("/{id}/status")
    public Result<String> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status, @RequestAttribute("userId") Long userId) {
        ColumnInfo columnInfo = columnInfoService.getById(id);
        if (columnInfo == null) {
            return Result.error(404, "专栏不存在");
        }
        // 只能操作自己的专栏
        if (!columnInfo.getCreatorId().equals(userId)) {
            return Result.error(403, "无权限操作该专栏");
        }
        columnInfo.setStatus(status);
        columnInfoService.updateById(columnInfo);
        return Result.success("操作成功");
    }
}
