package com.knowledge.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("orders")
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long columnId;
    private BigDecimal amount;
    /** 0待支付, 1已支付, 2已取消 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
