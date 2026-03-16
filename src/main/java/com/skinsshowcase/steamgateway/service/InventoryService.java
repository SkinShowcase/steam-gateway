package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.client.SteamClient;
import com.skinsshowcase.steamgateway.dto.InventoryItemResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.SteamAssetDto;
import com.skinsshowcase.steamgateway.dto.SteamAssetPropertiesDto;
import com.skinsshowcase.steamgateway.dto.SteamDescriptionDto;
import com.skinsshowcase.steamgateway.dto.SteamDescriptionItemDto;
import com.skinsshowcase.steamgateway.dto.SteamInventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.SteamTagDto;
import com.skinsshowcase.steamgateway.dto.StickerInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис инвентаря: запрос к Steam, маппинг в DTO API.
 * Float, pattern, коллекция и износ берутся из ответа Steam (asset_properties, descriptions, tags).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int PROPERTY_WEAR_RATING = 2;
    private static final int PROPERTY_PATTERN_TEMPLATE = 1;

    private final SteamClient steamClient;

    /**
     * Получить список предметов в инвентаре по Steam ID.
     * Float, pattern и коллекция извлекаются из JSON ответа Steam (без обращения к CSFloat).
     *
     * @param steamId   SteamID64
     * @param appId     App ID (730 — CS2)
     * @param contextId Context ID (2 — инвентарь CS2)
     */
    public Mono<InventoryResponseDto> getInventory(String steamId, int appId, int contextId) {
        return steamClient.getInventory(steamId, appId, contextId)
                .map(steam -> mapToResponse(steamId, appId, contextId, steam));
    }

    private InventoryResponseDto mapToResponse(String steamId, int appId, int contextId,
                                               SteamInventoryResponseDto steam) {
        var items = new ArrayList<InventoryItemResponseDto>();
        var assets = Optional.ofNullable(steam.getAssets()).orElse(List.of());
        var descriptions = Optional.ofNullable(steam.getDescriptions()).orElse(List.of());
        var assetPropertiesList = Optional.ofNullable(steam.getAssetProperties()).orElse(List.of());

        var descByKey = descriptions.stream()
                .filter(d -> d.getClassId() != null && d.getInstanceId() != null)
                .collect(Collectors.toMap(d -> d.getClassId() + "_" + d.getInstanceId(), d -> d, (a, b) -> a));

        var propsByAssetId = assetPropertiesList.stream()
                .filter(p -> p.getAssetId() != null)
                .collect(Collectors.toMap(SteamAssetPropertiesDto::getAssetId, p -> p, (a, b) -> a));

        for (var asset : assets) {
            var descKey = asset.getClassId() + "_" + asset.getInstanceId();
            var desc = descByKey.get(descKey);
            var props = propsByAssetId.get(asset.getId());
            items.add(mapItem(asset, desc, props));
        }

        log.debug("Mapped {} items for steamId={}, appId={}, contextId={}", items.size(), steamId, appId, contextId);
        return InventoryResponseDto.builder()
                .steamId(steamId)
                .appId(appId)
                .contextId(contextId)
                .items(items)
                .build();
    }

    private InventoryItemResponseDto mapItem(SteamAssetDto asset, SteamDescriptionDto desc,
                                              SteamAssetPropertiesDto assetProps) {
        var amount = parseAssetAmount(asset);
        var inspectLink = extractInspectLink(desc);
        var itemBuilder = InventoryItemResponseDto.builder()
                .assetId(asset.getId())
                .classId(asset.getClassId())
                .instanceId(asset.getInstanceId())
                .name(desc != null ? desc.getName() : null)
                .marketHashName(desc != null ? desc.getMarketHashName() : null)
                .type(desc != null ? desc.getType() : null)
                .amount(amount)
                .iconUrl(desc != null ? desc.getIconUrl() : null)
                .inspectLink(inspectLink);

        if (desc != null && desc.getDescriptionItems() != null && !desc.getDescriptionItems().isEmpty()) {
            var attrs = buildAttributesMap(desc.getDescriptionItems());
            fillItemAttributes(itemBuilder, attrs);
        }

        fillFromAssetProperties(itemBuilder, assetProps);
        itemBuilder.collectionName(extractCollectionFromTags(desc));

        return itemBuilder.build();
    }

    private void fillFromAssetProperties(InventoryItemResponseDto.InventoryItemResponseDtoBuilder itemBuilder,
                                         SteamAssetPropertiesDto assetProps) {
        if (assetProps == null || assetProps.getAssetProperties() == null) {
            return;
        }
        for (var prop : assetProps.getAssetProperties()) {
            if (prop.getPropertyId() == null) {
                continue;
            }
            if (prop.getPropertyId() == PROPERTY_WEAR_RATING && prop.getFloatValue() != null) {
                itemBuilder.floatValue(prop.getFloatValue());
                continue;
            }
            if (prop.getPropertyId() == PROPERTY_PATTERN_TEMPLATE) {
                var intVal = prop.getIntValue();
                if (intVal != null && !intVal.isBlank()) {
                    var parsed = parseInteger(intVal);
                    if (parsed != null) {
                        itemBuilder.paintSeed(parsed);
                        itemBuilder.pattern(parsed);
                    }
                }
            }
        }
    }

    private static String extractCollectionFromTags(SteamDescriptionDto desc) {
        if (desc == null || desc.getTags() == null) {
            return null;
        }
        for (SteamTagDto tag : desc.getTags()) {
            var cat = tag.getCategory();
            if (cat == null) {
                continue;
            }
            var c = cat.trim();
            if ("ItemSet".equalsIgnoreCase(c) || "Collection".equalsIgnoreCase(c)) {
                var name = tag.getLocalizedTagName();
                if (name != null && !name.isBlank()) {
                    return name.trim();
                }
            }
        }
        return null;
    }

    private static String extractInspectLink(SteamDescriptionDto desc) {
        if (desc == null || desc.getActions() == null) {
            return null;
        }
        for (var action : desc.getActions()) {
            var link = action.getLink();
            if (link == null || link.isBlank()) {
                continue;
            }
            if (link.contains("csgo_econ_action_preview") || link.contains("steam://rungame/730")) {
                return link;
            }
        }
        return null;
    }

    /**
     * Ключ атрибута: в Steam API используется name (exterior_wear, float и т.д.), при отсутствии — type.
     */
    private Map<String, String> buildAttributesMap(List<SteamDescriptionItemDto> descriptionItems) {
        var map = new LinkedHashMap<String, String>();
        for (var item : descriptionItems) {
            var key = (item.getName() != null && !item.getName().isBlank()) ? item.getName() : item.getType();
            if (key == null) {
                key = "";
            }
            key = key.trim();
            if (key.isEmpty() && item.getValue() != null) {
                continue;
            }
            map.put(key, item.getValue() != null ? item.getValue().trim() : "");
        }
        return map;
    }

    private void fillItemAttributes(InventoryItemResponseDto.InventoryItemResponseDtoBuilder itemBuilder,
                                    Map<String, String> attrs) {
        var extra = new LinkedHashMap<String, String>();

        for (var entry : attrs.entrySet()) {
            var type = entry.getKey();
            var value = entry.getValue();
            var typeLower = type.toLowerCase();

            if (isFloatType(typeLower)) {
                itemBuilder.floatValue(parseDouble(value));
                continue;
            }
            if (isPaintSeedType(typeLower)) {
                itemBuilder.paintSeed(parseInteger(value));
                continue;
            }
            if (isPaintIndexType(typeLower)) {
                itemBuilder.paintIndex(parseLong(value));
                continue;
            }
            if (isWearNameType(typeLower)) {
                itemBuilder.wearName(normalizeWearValue(value));
                continue;
            }

            extra.put(type, value);
        }

        var stickers = parseStickersFromAttributes(attrs);
        if (stickers != null && !stickers.isEmpty()) {
            itemBuilder.stickers(stickers);
            extra.keySet().removeIf(k -> "stickers".equalsIgnoreCase(k) || "sticker".equalsIgnoreCase(k) || "sticker_info".equalsIgnoreCase(k));
        }
        if (!extra.isEmpty()) {
            itemBuilder.extraAttributes(extra);
        }
    }

    private static boolean isFloatType(String typeLower) {
        return "float".equals(typeLower);
    }

    private static boolean isPaintSeedType(String typeLower) {
        return "paint seed".equals(typeLower) || "paintseed".equals(typeLower);
    }

    private static boolean isPaintIndexType(String typeLower) {
        return "paint index".equals(typeLower) || "paintindex".equals(typeLower);
    }

    private static boolean isWearNameType(String typeLower) {
        return "wear".equals(typeLower) || "exterior".equals(typeLower) || "exterior_wear".equals(typeLower);
    }

    /** Убирает префикс "Exterior: " из значения износа из Steam (например "Exterior: Factory New" → "Factory New"). */
    private static String normalizeWearValue(String value) {
        if (value == null) {
            return null;
        }
        var v = value.trim();
        if (v.startsWith("Exterior:")) {
            return v.substring(9).trim();
        }
        return v;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.trace("Invalid float value: {}", value);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.trace("Invalid integer value: {}", value);
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.trace("Invalid long value: {}", value);
            return null;
        }
    }

    private List<StickerInfoDto> parseStickersFromAttributes(Map<String, String> attrs) {
        for (var entry : attrs.entrySet()) {
            var type = entry.getKey();
            var value = entry.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            if ("sticker_info".equalsIgnoreCase(type)) {
                var list = StickerParser.parseFromStickerInfo(value);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            }
            if ("stickers".equalsIgnoreCase(type) || "sticker".equalsIgnoreCase(type)) {
                var list = parseStickersValue(value);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            }
        }
        return null;
    }

    private List<StickerInfoDto> parseStickersValue(String value) {
        return StickerParser.tryParse(value);
    }

    private int parseAssetAmount(SteamAssetDto asset) {
        if (asset.getAmount() == null) {
            return 1;
        }
        try {
            return Integer.parseInt(asset.getAmount());
        } catch (NumberFormatException e) {
            log.trace("Invalid amount for asset id={}: {}", asset.getId(), asset.getAmount());
            return 1;
        }
    }
}
