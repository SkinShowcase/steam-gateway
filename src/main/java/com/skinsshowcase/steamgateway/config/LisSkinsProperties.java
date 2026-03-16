package com.skinsshowcase.steamgateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Настройки клиента lis-skins.com: URL экспорта CS2, таймауты, retry.
 */
@ConfigurationProperties(prefix = "lis-skins")
@Validated
@Getter
@Setter
public class LisSkinsProperties {

    /**
     * URL полного экспорта CS2 (JSON). Например: https://lis-skins.com/market_export_json/api_csgo_full.json
     */
    @NotBlank
    private String exportUrl = "https://lis-skins.com/market_export_json/api_csgo_full.json";

    @Positive
    private long connectTimeoutMs = 5_000;

    @Positive
    private long readTimeoutMs = 30_000;

    /**
     * Максимум повторов при ошибке/таймауте.
     */
    @Positive
    private int maxRetries = 3;

    /**
     * Максимальный размер буфера ответа в байтах (для большого JSON api_csgo_full.json).
     * По умолчанию 1 GB.
     */
    @Positive
    private int maxInMemorySizeBytes = 1024 * 1024 * 1024;
}
