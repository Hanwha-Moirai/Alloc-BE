package com.moirai.alloc.auth.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.sync")
public class AuthSyncProperties {
    private String baseUrl;
    private String syncPath = "/api/internal/users/sync";
}
