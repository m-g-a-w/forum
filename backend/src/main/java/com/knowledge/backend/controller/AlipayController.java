package com.knowledge.backend.controller;

import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.knowledge.backend.common.Result;
import com.knowledge.backend.entity.RechargeRecord;
import com.knowledge.backend.service.RechargeRecordService;
import com.knowledge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alipay")
public class AlipayController {

    @Autowired
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private UserService userService;

    @Value("${alipay.sandbox:false}")
    private boolean sandbox;

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", sandbox);
        result.put("mode", sandbox ? "沙箱环境" : "生产环境");
        return Result.success(result);
    }
}
