package com.knowledge.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("column_info")
public class ColumnInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long creatorId;
    private String title;
    private String description;
    private String cover;
    private BigDecimal price;
    /** 0下架/草稿, 1上架/已发布 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /** 非数据库字段：文章数量 */
    @TableField(exist = false)
    private Integer articleCount;
}
