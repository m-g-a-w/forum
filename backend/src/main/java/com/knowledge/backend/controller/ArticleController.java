package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Article;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.service.ArticleService;
import com.knowledge.backend.service.ColumnInfoService;
import com.knowledge.backend.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ColumnInfoService columnInfoService;

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/publish")
    public Result<Article> publish(@RequestBody Article article, @RequestAttribute("userId") Long userId) {
        // 校验专栏是否属于本人
        ColumnInfo column = columnInfoService.getById(article.getColumnId());
        if (column == null || !column.getCreatorId().equals(userId)) {
            return Result.error(403, "无权在该专栏发布文章");
        }
        if (article.getStatus() == null) {
            article.setStatus(1);
        }
        articleService.saveOrUpdate(article);
        return Result.success(article);
    }

    @GetMapping("/list/{columnId}")
    public Result<List<Article>> listByColumn(@PathVariable("columnId") Long columnId) {
        // 简单返回文章列表（这里暂不含正文或脱敏处理，由前端/后续逻辑控制，如果是免费则直接给，如果是收费判断是否购买，为了简单起见，列表不返回正文）
        List<Article> list = articleService.lambdaQuery()
                .eq(Article::getColumnId, columnId)
                .eq(Article::getStatus, 1)
                .select(Article::getId, Article::getColumnId, Article::getChapterId, Article::getTitle, Article::getIsFree, Article::getSortOrder, Article::getCreateTime)
                .orderByAsc(Article::getSortOrder)
                .list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable("id") Long id, @RequestAttribute(value = "userId", required = false) Long userId) {
        Article article = articleService.getById(id);
        if (article == null || article.getStatus() == 0) {
            return Result.error(404, "文章不存在或已下架");
        }
        // 检查是否需要订阅
        ColumnInfo column = columnInfoService.getById(article.getColumnId());
        if (column != null && column.getPrice() != null && column.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            // 专栏是付费的，检查用户是否已订阅
            if (userId == null || !subscriptionService.checkSubscribe(userId, article.getColumnId())) {
                // 未订阅且不是免费文章，隐藏正文
                if (article.getIsFree() == null || article.getIsFree() != 1) {
                    article.setContent("【付费内容】请先订阅该专栏以阅读完整内容");
                    article.setPaywall(true);
                }
            }
        }
        return Result.success(article);
    }
}
