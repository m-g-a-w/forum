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
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private String bio;
    /** 0普通用户, 1创作者, 2管理员 */
    private Integer role;
    private BigDecimal balance;
    /** 0禁用, 1正常 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
