package com.skinsshowcase.steamgateway.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки клиента lis-skins.com: URL экспорта CS2, таймауты, retry.
 */
@Component
@Validated
@Getter
public class LisSkinsProperties {

    private final String exportUrl;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final int maxRetries;
    private final int maxInMemorySizeBytes;

    public LisSkinsProperties(
            @NotBlank @Value("${lis-skins.export-url}") String exportUrl,
            @Positive @Value("${lis-skins.connect-timeout-ms}") long connectTimeoutMs,
            @Positive @Value("${lis-skins.read-timeout-ms}") long readTimeoutMs,
            @Positive @Value("${lis-skins.max-retries}") int maxRetries,
            @Positive @Value("${lis-skins.max-in-memory-size-bytes}") int maxInMemorySizeBytes) {
        this.exportUrl = exportUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.maxRetries = maxRetries;
        this.maxInMemorySizeBytes = maxInMemorySizeBytes;
    }
}
