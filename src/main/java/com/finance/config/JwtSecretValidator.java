package com.finance.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fails fast on weak or missing JWT secret outside tests. Test profile supplies a key via
 * {@code application-test.yml}.
 */
@Component
@Profile("!test")
public class JwtSecretValidator {

    private final AppJwtProperties jwtProperties;

    public JwtSecretValidator(AppJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void validate() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is missing or empty. Copy .env.example to .env and set a strong secret (≥ 32 characters).");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is too short (minimum 32 characters for HS256). Use a long random value.");
        }
    }
}
