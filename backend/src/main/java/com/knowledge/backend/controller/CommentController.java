package com.knowledge.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Article;
import com.knowledge.backend.entity.Comment;
import com.knowledge.backend.entity.User;
import com.knowledge.backend.service.ArticleService;
import com.knowledge.backend.service.CommentService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @PostMapping("/publish")
    public Result<Comment> publish(@RequestBody Comment comment, @RequestAttribute("userId") Long userId) {
        comment.setUserId(userId);
        commentService.save(comment);
        return Result.success(comment);
    }

    @GetMapping("/list/{articleId}")
    public Result<List<Comment>> list(@PathVariable("articleId") Long articleId) {
        List<Comment> list = commentService.lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime)
                .list();
        return Result.success(list);
    }

    // ==================== 管理员评论管理 ====================

    /**
     * 获取所有评论列表（带用户信息和文章标题）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);

        List<Comment> comments = commentService.list(wrapper);
        List<User> users = userService.list();
        List<Article> articles = articleService.list();

        // 构建 Map，处理可能的重复 key
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Article> articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a, (a, b) -> a));

        List<Map<String, Object>> result = comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());
            map.put("articleId", comment.getArticleId());
            map.put("userId", comment.getUserId());

            User user = userMap.get(comment.getUserId());
            if (user != null) {
                map.put("username", user.getUsername());
                map.put("userAvatar", user.getAvatar());
            }

            Article article = articleMap.get(comment.getArticleId());
            if (article != null) {
                map.put("articleTitle", article.getTitle());
            }

            // 关键词过滤
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                boolean match = (map.get("username") != null && map.get("username").toString().toLowerCase().contains(kw))
                        || (map.get("content") != null && map.get("content").toString().toLowerCase().contains(kw))
                        || (map.get("articleTitle") != null && map.get("articleTitle").toString().toLowerCase().contains(kw));
                if (!match) return null;
            }

            return map;
        }).filter(m -> m != null).collect(Collectors.toList());

        // 分页
        int total = result.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> paged = start < total ? result.subList(start, end) : List.of();

        Map<String, Object> response = new HashMap<>();
        response.put("list", paged);
        response.put("total", total);

        return Result.success(response);
    }

    /**
     * 获取评论统计
     */
    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> adminStats() {
        long total = commentService.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("today", commentService.lambdaQuery()
                .apply("DATE(create_time) = CURDATE()").count());

        return Result.success(stats);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/admin/{id}")
    public Result<String> adminDelete(@PathVariable("id") Long id) {
        boolean success = commentService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error(404, "评论不存在");
        }
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/admin/batch")
    public Result<String> adminBatchDelete(@RequestBody List<Long> ids) {
        boolean success = commentService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error(400, "批量删除失败");
        }
    }
}
