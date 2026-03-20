package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.Chapter;
import com.knowledge.backend.mapper.ChapterMapper;
import com.knowledge.backend.service.ChapterService;
import org.springframework.stereotype.Service;

@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {
}
