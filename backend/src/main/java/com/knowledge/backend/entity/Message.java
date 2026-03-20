package com.knowledge.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_wall")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private LocalDateTime createTime;
}
