package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.client.ItemsCatalogClient;
import com.skinsshowcase.steamgateway.metrics.SteamGatewayMetrics;
import com.skinsshowcase.steamgateway.dto.InventoryItemResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.ItemCatalogPriceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Разрешение цен каталога с учётом кеша по classId и пакетного запроса к items.
 */
@Service
@RequiredArgsConstructor
public class ItemCatalogPriceResolutionService {

    private final ItemsCatalogClient itemsCatalogClient;
    private final SteamGatewayCacheSupport steamGatewayCacheSupport;
    private final SteamGatewayMetrics steamGatewayMetrics;

    public Mono<InventoryResponseDto> enrichInventoryWithPrices(InventoryResponseDto inventory) {
        var classIds = collectDistinctClassIds(inventory);
        if (classIds.isEmpty()) {
            return Mono.just(inventory);
        }
        return resolvePriceMap(classIds).map(priceMap -> applyPricesToInventory(inventory, priceMap));
    }

    private static Set<String> collectDistinctClassIds(InventoryResponseDto inventory) {
        var out = new LinkedHashSet<String>();
        var items = inventory.getItems();
        if (items == null) {
            return out;
        }
        for (var item : items) {
            addClassIdIfPresent(out, item);
        }
        return out;
    }

    private static void addClassIdIfPresent(Set<String> out, InventoryItemResponseDto item) {
        var cid = item.getClassId();
        if (cid == null || cid.isBlank()) {
            return;
        }
        out.add(cid.trim());
    }

    private Mono<Map<String, ItemCatalogPriceDto>> resolvePriceMap(Set<String> classIds) {
        var cached = loadCachedPrices(classIds);
        var missing = computeMissing(classIds, cached);
        if (missing.isEmpty()) {
            steamGatewayMetrics.recordCatalogPriceStrategy("cache_only");
            return Mono.just(cached);
        }
        steamGatewayMetrics.recordCatalogPriceStrategy("batch_fetch");
        var missingCount = missing.size();
        return fetchMissingAndMerge(cached, missing)
                .doOnSuccess(m -> steamGatewayMetrics.recordCatalogBatchClassIds(missingCount));
    }

    private Map<String, ItemCatalogPriceDto> loadCachedPrices(Set<String> classIds) {
        var map = new LinkedHashMap<String, ItemCatalogPriceDto>();
        for (var id : classIds) {
            var opt = steamGatewayCacheSupport.getCachedCatalogPrice(id);
            if (opt.isPresent()) {
                map.put(id, opt.get());
            }
        }
        return map;
    }

    private static List<String> computeMissing(Set<String> classIds, Map<String, ItemCatalogPriceDto> cached) {
        var list = new ArrayList<String>();
        for (var id : classIds) {
            if (!cached.containsKey(id)) {
                list.add(id);
            }
        }
        return list;
    }

    private Mono<Map<String, ItemCatalogPriceDto>> fetchMissingAndMerge(Map<String, ItemCatalogPriceDto> cached,
                                                                        List<String> missing) {
        return itemsCatalogClient.fetchCatalogPricesBatch(missing).map(fetched -> mergeAndCache(cached, fetched));
    }

    private Map<String, ItemCatalogPriceDto> mergeAndCache(Map<String, ItemCatalogPriceDto> cached,
                                                           Map<String, ItemCatalogPriceDto> fetched) {
        var result = new LinkedHashMap<>(cached);
        for (var entry : fetched.entrySet()) {
            var id = entry.getKey();
            var dto = entry.getValue();
            if (dto == null) {
                continue;
            }
            if (dto.getMinPriceUsd() != null) {
                steamGatewayCacheSupport.putCatalogPrice(id, dto);
            }
            result.put(id, dto);
        }
        return result;
    }

    private static InventoryResponseDto applyPricesToInventory(InventoryResponseDto inventory,
                                                               Map<String, ItemCatalogPriceDto> priceMap) {
        var items = inventory.getItems();
        if (items == null) {
            return inventory;
        }
        for (var item : items) {
            applyPriceToItem(item, priceMap);
        }
        return inventory;
    }

    private static void applyPriceToItem(InventoryItemResponseDto item, Map<String, ItemCatalogPriceDto> priceMap) {
        var cid = item.getClassId();
        if (cid == null || cid.isBlank()) {
            return;
        }
        var dto = priceMap.get(cid.trim());
        if (dto == null || dto.getMinPriceUsd() == null) {
            return;
        }
        item.setCatalogMinPriceUsd(dto.getMinPriceUsd());
    }
}
