package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.config.SteamGatewayCacheNames;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.ItemCatalogPriceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Чтение/запись кешей инвентаря и цен каталога.
 */
@Service
@RequiredArgsConstructor
public class SteamGatewayCacheSupport {

    private final CacheManager cacheManager;

    public Optional<InventoryResponseDto> getCachedInventory(String steamId, int appId, int contextId) {
        var cache = cacheManager.getCache(SteamGatewayCacheNames.STEAM_INVENTORY);
        if (cache == null) {
            return Optional.empty();
        }
        var key = inventoryKey(steamId, appId, contextId);
        var wrapper = cache.get(key);
        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }
        var value = wrapper.get();
        if (value instanceof InventoryResponseDto dto) {
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    public void putInventory(String steamId, int appId, int contextId, InventoryResponseDto dto) {
        var cache = cacheManager.getCache(SteamGatewayCacheNames.STEAM_INVENTORY);
        if (cache == null) {
            return;
        }
        cache.put(inventoryKey(steamId, appId, contextId), dto);
    }

    public Optional<ItemCatalogPriceDto> getCachedCatalogPrice(String classId) {
        var cache = cacheManager.getCache(SteamGatewayCacheNames.CATALOG_PRICE);
        if (cache == null) {
            return Optional.empty();
        }
        var wrapper = cache.get(classId);
        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }
        var value = wrapper.get();
        if (value instanceof ItemCatalogPriceDto dto) {
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    public void putCatalogPrice(String classId, ItemCatalogPriceDto dto) {
        var cache = cacheManager.getCache(SteamGatewayCacheNames.CATALOG_PRICE);
        if (cache == null) {
            return;
        }
        cache.put(classId, dto);
    }

    public void evictAllSteamGatewayCaches() {
        evictNamed(SteamGatewayCacheNames.STEAM_INVENTORY);
        evictNamed(SteamGatewayCacheNames.CATALOG_PRICE);
    }

    private void evictNamed(String name) {
        var cache = cacheManager.getCache(name);
        if (cache != null) {
            cache.clear();
        }
    }

    private static String inventoryKey(String steamId, int appId, int contextId) {
        return steamId + ":" + appId + ":" + contextId;
    }
}
