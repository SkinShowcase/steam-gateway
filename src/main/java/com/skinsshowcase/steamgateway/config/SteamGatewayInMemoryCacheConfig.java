package com.skinsshowcase.steamgateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory кеш, если Redis отключён (локальная разработка без redis).
 */
@Configuration
@ConditionalOnProperty(name = "steam-gateway.cache.redis-enabled", havingValue = "false")
public class SteamGatewayInMemoryCacheConfig {

    @Bean
    public CacheManager steamGatewayInMemoryCacheManager() {
        return new ConcurrentMapCacheManager(SteamGatewayCacheNames.STEAM_INVENTORY, SteamGatewayCacheNames.CATALOG_PRICE);
    }
}
