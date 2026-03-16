package com.skinsshowcase.steamgateway.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Duration;

/**
 * Настройки клиента Steam (URL, таймауты, retry).
 */
@ConfigurationProperties(prefix = "steam.client")
@Validated
@Getter
@Setter
public class SteamClientProperties {

    @NotBlank
    private String baseUrl = "https://steamcommunity.com";

    /**
     * Шаблон пути: /profiles/{steamId}/inventory/json/{appId}/{contextId}
     */
    @NotBlank
    private String inventoryPathTemplate = "/inventory/{steamId}/{appId}/{contextId}"; //"/profiles/{steamId}/inventory/json/{appId}/{contextId}";

    /**
     * Шаблон пути Steam Market search (CS2 appid=730): /market/search/render/?norender=1&appid=730&currency=1&count=100&start=0
     */
    @NotBlank
    private String marketSearchPathTemplate = "/market/search/render/?norender=1&appid=730&currency=1&count={count}&start={start}";

    @Positive
    private long connectTimeoutMs = 5_000;

    @Positive
    private long readTimeoutMs = 15_000;

    public Duration getConnectTimeout() {
        return Duration.ofMillis(connectTimeoutMs);
    }

    public Duration getReadTimeout() {
        return Duration.ofMillis(readTimeoutMs);
    }
}
