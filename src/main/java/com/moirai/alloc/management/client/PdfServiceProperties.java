package com.moirai.alloc.management.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "pdf.service")
public class PdfServiceProperties {
    private String baseUrl;
}
