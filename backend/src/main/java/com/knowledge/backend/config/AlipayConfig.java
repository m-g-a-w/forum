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
        config.appId = appId != null ? appId.trim() : "";
        config.merchantPrivateKey = normalizeKeyMaterial(privateKey);
        config.alipayPublicKey = normalizeKeyMaterial(alipayPublicKey);
        config.notifyUrl = notifyUrl != null ? notifyUrl.trim() : "";

        Factory.setOptions(config);
        System.out.println(">> [INFO] Alipay initialized. Sandbox: " + sandbox + ", AppId: " + appId);
        System.out.println(">> [DEBUG] PrivateKey length: " + (config.merchantPrivateKey != null ? config.merchantPrivateKey.length() : "null"));
        System.out.println(">> [DEBUG] AlipayPublicKey length: " + (config.alipayPublicKey != null ? config.alipayPublicKey.length() : "null"));
    }

    /**
     * 去掉 PEM 头尾与空白；EasySDK 需要无头尾的纯 Base64 一行。
     */
    static String normalizeKeyMaterial(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (!trimmed.contains("BEGIN")) {
            return trimmed.replaceAll("\\s+", "");
        }
        StringBuilder sb = new StringBuilder();
        for (String line : trimmed.split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("-----")) {
                continue;
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
