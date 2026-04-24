package com.skinsshowcase.steamgateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Внутренние вызовы (инвалидация кеша после sync каталога).
 */
@Component
@Getter
public class SteamGatewayInternalProperties {

    private final String cacheEvictToken;

    public SteamGatewayInternalProperties(
            @Value("${steam-gateway.internal.cache-evict-token}") String cacheEvictToken) {
        this.cacheEvictToken = cacheEvictToken;
    }
}
