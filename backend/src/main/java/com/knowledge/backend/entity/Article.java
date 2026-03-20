package com.knowledge.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("article")
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long columnId;
    private Long chapterId;
    private String title;
    private String content;
    /** 是否免费试看: 0否, 1是 */
    private Integer isFree;
    private Integer sortOrder;
    /** 状态: 0草稿, 1发布 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
