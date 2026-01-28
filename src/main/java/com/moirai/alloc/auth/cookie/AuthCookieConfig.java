package com.moirai.alloc.auth.cookie;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthCookieProperties.class)
public class AuthCookieConfig {
}
