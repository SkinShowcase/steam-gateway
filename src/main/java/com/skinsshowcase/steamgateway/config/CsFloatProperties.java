package com.skinsshowcase.steamgateway.config;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки интеграции с CSFloat Inspect API (https://github.com/csfloat/inspect).
 * Используется для получения float, paint seed и прочих характеристик скинов по inspect-ссылке.
 */
@Component
@Validated
@Getter
public class CsFloatProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final int bulkMaxLinks;
    private final String bulkKey;

    public CsFloatProperties(
            @Value("${csfloat.inspect.enabled}") boolean enabled,
            @Value("${csfloat.inspect.base-url}") String baseUrl,
            @Positive @Value("${csfloat.inspect.connect-timeout-ms}") long connectTimeoutMs,
            @Positive @Value("${csfloat.inspect.read-timeout-ms}") long readTimeoutMs,
            @Positive @Value("${csfloat.inspect.bulk-max-links}") int bulkMaxLinks,
            @Value("${csfloat.inspect.bulk-key}") String bulkKey) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.bulkMaxLinks = bulkMaxLinks;
        this.bulkKey = bulkKey;
    }
}
