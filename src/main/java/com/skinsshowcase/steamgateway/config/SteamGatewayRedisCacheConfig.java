package com.skinsshowcase.steamgateway.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Кеш Redis: инвентарь и цены каталога, TTL 1 час.
 */
@Configuration
@ConditionalOnProperty(name = "steam-gateway.cache.redis-enabled", havingValue = "true", matchIfMissing = true)
public class SteamGatewayRedisCacheConfig {

    private static final Duration TTL = Duration.ofHours(1);

    @Bean
    public CacheManager steamGatewayCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        var redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        var serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .initialCacheNames(java.util.Set.of(SteamGatewayCacheNames.STEAM_INVENTORY, SteamGatewayCacheNames.CATALOG_PRICE))
                .build();
    }
}
