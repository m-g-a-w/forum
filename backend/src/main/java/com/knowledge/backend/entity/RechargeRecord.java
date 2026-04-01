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
@TableName("recharge_record")
public class RechargeRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /** 充值单号 */
    private String rechargeNo;
    
    /** 充值金额 */
    private BigDecimal amount;
    
    /** 支付方式: alipay-支付宝, wechat-微信, bankcard-银行卡 */
    private String payMethod;
    
    /** 充值状态: 0-待支付, 1-已成功, 2-已取消 */
    private Integer status;
    
    /** 支付成功时间 */
    private LocalDateTime payTime;
    
    private LocalDateTime createTime;
}
