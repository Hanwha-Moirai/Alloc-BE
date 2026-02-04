package com.moirai.alloc.auth.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AuthSyncProperties.class)
public class AuthSyncClientConfig {

    @Bean
    WebClient authSyncWebClient(AuthSyncProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }
}
