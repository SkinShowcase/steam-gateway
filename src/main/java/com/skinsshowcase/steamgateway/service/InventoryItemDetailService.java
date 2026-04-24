package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.client.ItemsCatalogClient;
import com.skinsshowcase.steamgateway.dto.InventoryItemDetailResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryItemResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.ItemCatalogPriceDto;
import com.skinsshowcase.steamgateway.exception.InventoryItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Один предмет инвентаря по assetId + classId и цена из каталога items.
 */
@Service
@RequiredArgsConstructor
public class InventoryItemDetailService {

    private final InventoryService inventoryService;
    private final ItemsCatalogClient itemsCatalogClient;
    private final OwnerDisplayLabelService ownerDisplayLabelService;

    public Mono<InventoryItemDetailResponseDto> getItemDetail(String steamId, String assetId, String classId,
                                                             int appId, int contextId) {
        return Mono.zip(
                inventoryService.getInventory(steamId, appId, contextId),
                ownerDisplayLabelService.resolveOwnerLabelHolderMono(steamId)
        ).flatMap(tuple -> toDetailMono(tuple.getT1(), steamId, assetId, classId, appId, contextId,
                tuple.getT2().orElse(null)));
    }

    private Mono<InventoryItemDetailResponseDto> toDetailMono(InventoryResponseDto inv, String steamId,
                                                             String assetId, String classId,
                                                             int appId, int contextId, String personaName) {
        var matched = findItemInInventory(inv, assetId, classId);
        if (matched == null) {
            return Mono.error(new InventoryItemNotFoundException(
                    "No inventory item for assetId=" + assetId + ", classId=" + classId));
        }
        return attachCatalogPrice(steamId, appId, contextId, matched, personaName);
    }

    private Mono<InventoryItemDetailResponseDto> attachCatalogPrice(String steamId, int appId, int contextId,
                                                                   InventoryItemResponseDto item,
                                                                   String personaName) {
        var cid = item.getClassId();
        if (cid == null || cid.isBlank()) {
            return Mono.just(buildDetail(steamId, appId, contextId, item, null, personaName));
        }
        return itemsCatalogClient.fetchCatalogPriceByClassId(cid.trim())
                .map(price -> buildDetail(steamId, appId, contextId, item, price, personaName))
                .defaultIfEmpty(buildDetail(steamId, appId, contextId, item, null, personaName));
    }

    private static InventoryItemDetailResponseDto buildDetail(String steamId, int appId, int contextId,
                                                             InventoryItemResponseDto item,
                                                             ItemCatalogPriceDto catalogPrice,
                                                             String personaName) {
        return InventoryItemDetailResponseDto.builder()
                .steamId(steamId)
                .personaName(personaName)
                .appId(appId)
                .contextId(contextId)
                .item(item)
                .catalogPrice(catalogPrice)
                .build();
    }

    private static InventoryItemResponseDto findItemInInventory(InventoryResponseDto inv, String assetId, String classId) {
        if (inv.getItems() == null) {
            return null;
        }
        for (var it : inv.getItems()) {
            if (matchesAssetAndClass(it, assetId, classId)) {
                return it;
            }
        }
        return null;
    }

    private static boolean matchesAssetAndClass(InventoryItemResponseDto it, String assetId, String classId) {
        if (it.getAssetId() == null || it.getClassId() == null) {
            return false;
        }
        return it.getAssetId().equals(assetId) && it.getClassId().equals(classId);
    }
}
