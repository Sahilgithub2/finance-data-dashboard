package com.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class AppJwtProperties {

    /**
     * HS256 signing key; set {@code APP_JWT_SECRET} in {@code .env} or the environment (≥ 32 bytes
     * recommended for HMAC-SHA256).
     */
    private String secret = "";

    private long expirationMs = 86_400_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
