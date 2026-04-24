package com.skinsshowcase.steamgateway.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Настройки клиента Steam (URL, таймауты, retry).
 */
@Component
@Validated
@Getter
public class SteamClientProperties {

    private final String baseUrl;
    private final String inventoryPathTemplate;
    private final String marketSearchPathTemplate;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;

    public SteamClientProperties(
            @NotBlank @Value("${steam.client.base-url}") String baseUrl,
            @NotBlank @Value("${steam.client.inventory-path-template}") String inventoryPathTemplate,
            @NotBlank @Value("${steam.client.market-search-path-template}") String marketSearchPathTemplate,
            @Positive @Value("${steam.client.connect-timeout-ms}") long connectTimeoutMs,
            @Positive @Value("${steam.client.read-timeout-ms}") long readTimeoutMs) {
        this.baseUrl = baseUrl;
        this.inventoryPathTemplate = inventoryPathTemplate;
        this.marketSearchPathTemplate = marketSearchPathTemplate;
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
