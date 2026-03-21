package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Article;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.service.ArticleService;
import com.knowledge.backend.service.ColumnInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/column")
public class ColumnController {

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private ArticleService articleService;

    @PostMapping("/create")
    public Result<ColumnInfo> create(@RequestBody ColumnInfo columnInfo, @RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role) {
        if (role != 1 && role != 2) {
            return Result.error(403, "无权限创建专栏");
        }
        columnInfo.setCreatorId(userId);
        columnInfo.setStatus(1); // 默认上架
        columnInfoService.save(columnInfo);
        return Result.success(columnInfo);
    }

    @GetMapping("/list/public")
    public Result<List<ColumnInfo>> listPublic() {
        List<ColumnInfo> list = columnInfoService.lambdaQuery()
                .eq(ColumnInfo::getStatus, 1)
                .orderByDesc(ColumnInfo::getCreateTime)
                .list();
        return Result.success(list);
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
