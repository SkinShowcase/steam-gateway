package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.config.SteamGatewayInternalProperties;
import com.skinsshowcase.steamgateway.service.SteamGatewayCacheSupport;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Внутренняя инвалидация кеша после синхронизации каталога с lis-skins.
 */
@Hidden
@RestController
@RequestMapping("/internal/v1/cache")
@RequiredArgsConstructor
public class InternalCacheEvictController {

    private static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";

    private final SteamGatewayInternalProperties internalProperties;
    private final SteamGatewayCacheSupport steamGatewayCacheSupport;

    @PostMapping("/evict")
    public ResponseEntity<Void> evictCaches(
            @RequestHeader(value = HEADER_INTERNAL_TOKEN, required = false) String token) {
        if (!isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        steamGatewayCacheSupport.evictAllSteamGatewayCaches();
        return ResponseEntity.noContent().build();
    }

    private boolean isTokenValid(String token) {
        var expected = internalProperties.getCacheEvictToken();
        if (!StringUtils.hasText(expected)) {
            return false;
        }
        return expected.equals(token);
    }
}
