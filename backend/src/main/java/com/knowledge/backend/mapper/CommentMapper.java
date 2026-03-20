package com.knowledge.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.backend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
