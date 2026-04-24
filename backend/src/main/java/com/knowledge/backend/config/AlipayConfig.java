package com.knowledge.backend.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AlipayConfig {

    @Value("${alipay.app-id:}")
    private String appId;

    @Value("${alipay.private-key:}")
    private String privateKey;

    @Value("${alipay.alipay-public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.notify-url:}")
    private String notifyUrl;

    @Value("${alipay.sandbox:false}")
    private boolean sandbox;

    @PostConstruct
    public void initAlipay() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = sandbox ? "openapi-sandbox.dl.alipaydev.com" : "openapi.alipay.com";
        config.signType = "RSA2";
        config.appId = appId;
        config.merchantPrivateKey = privateKey;
        config.alipayPublicKey = alipayPublicKey;
        config.notifyUrl = notifyUrl;

        Factory.setOptions(config);
        System.out.println(">> [INFO] Alipay initialized. Sandbox: " + sandbox + ", AppId: " + appId);
        System.out.println(">> [DEBUG] PrivateKey length: " + (privateKey != null ? privateKey.length() : "null"));
    }
}
