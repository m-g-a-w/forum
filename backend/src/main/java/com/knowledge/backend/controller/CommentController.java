package com.knowledge.backend.controller;

import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.Comment;
import com.knowledge.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

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
}
