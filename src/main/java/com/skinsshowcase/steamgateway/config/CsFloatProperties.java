package com.skinsshowcase.steamgateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

/**
 * Настройки интеграции с CSFloat Inspect API (https://github.com/csfloat/inspect).
 * Используется для получения float, paint seed и прочих характеристик скинов по inspect-ссылке.
 */
@ConfigurationProperties(prefix = "csfloat.inspect")
@Validated
@Getter
@Setter
public class CsFloatProperties {

    /**
     * Включить обогащение инвентаря через CSFloat Inspect API.
     * Если false или URL не задан — возвращаются только данные из Steam (без вызова CSFloat).
     */
    private boolean enabled = false;

    /**
     * Base URL сервиса CSFloat Inspect (например http://localhost:80 или https://api.csgofloat.com).
     * Обязателен при enabled=true.
     */
    private String baseUrl = "";

    @Positive
    private long connectTimeoutMs = 5_000;

    @Positive
    private long readTimeoutMs = 30_000;

    /**
     * Максимум ссылок в одном bulk-запросе (лимит сервиса — обычно не более нескольких десятков).
     */
    @Positive
    private int bulkMaxLinks = 50;

    /**
     * Секрет для POST /bulk (если в конфиге бота csfloat задан bulk_key — укажите тот же ключ здесь).
     */
    private String bulkKey = "";
}
