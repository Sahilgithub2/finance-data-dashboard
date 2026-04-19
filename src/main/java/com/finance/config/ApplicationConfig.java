package com.finance.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppJwtProperties.class, AppRateLimitProperties.class})
public class ApplicationConfig {}
