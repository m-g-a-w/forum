package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.Article;
import com.knowledge.backend.mapper.ArticleMapper;
import com.knowledge.backend.service.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
}
