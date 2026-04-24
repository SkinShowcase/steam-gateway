package com.skinsshowcase.steamgateway.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Базовый URL сервиса items (каталог цен по classid).
 */
@Component
@Validated
@Getter
public class ItemsCatalogProperties {

    private final String baseUrl;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;

    public ItemsCatalogProperties(
            @NotBlank @Value("${items-catalog.base-url}") String baseUrl,
            @Positive @Value("${items-catalog.connect-timeout-ms}") long connectTimeoutMs,
            @Positive @Value("${items-catalog.read-timeout-ms}") long readTimeoutMs) {
        this.baseUrl = baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public Duration getConnectTimeout() {
        return Duration.ofMillis(connectTimeoutMs);
    }

    public Duration getReadTimeout() {
        return Duration.ofMillis(readTimeoutMs);
    }
}
