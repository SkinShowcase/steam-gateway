package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.dto.SteamDescriptionDto;
import com.skinsshowcase.steamgateway.dto.SteamTagDto;

/**
 * Оставляем только предметы, доступные для обмена; исключаем медали и прочие non-tradable.
 */
public final class InventoryTradableMedalFilter {

    private static final int TRADABLE_YES = 1;

    private InventoryTradableMedalFilter() {
    }

    public static boolean isInventoryItemIncluded(SteamDescriptionDto description) {
        if (description == null) {
            return false;
        }
        if (!isTradable(description)) {
            return false;
        }
        return !isMedalOrExcludedCollectible(description);
    }

    private static boolean isTradable(SteamDescriptionDto description) {
        var t = description.getTradable();
        return t != null && t == TRADABLE_YES;
    }

    private static boolean isMedalOrExcludedCollectible(SteamDescriptionDto description) {
        if (matchesMedalText(description.getType())) {
            return true;
        }
        if (matchesMedalText(description.getName())) {
            return true;
        }
        if (matchesMedalText(description.getMarketHashName())) {
            return true;
        }
        return tagsIndicateMedal(description);
    }

    private static boolean matchesMedalText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        var lower = text.toLowerCase();
        if (lower.contains("service medal")) {
            return true;
        }
        if (lower.contains("coin") && lower.contains("collectible")) {
            return true;
        }
        if (lower.contains("medal")) {
            return true;
        }
        if (lower.contains("operation coin")) {
            return true;
        }
        return false;
    }

    private static boolean tagsIndicateMedal(SteamDescriptionDto description) {
        var tags = description.getTags();
        if (tags == null) {
            return false;
        }
        for (SteamTagDto tag : tags) {
            if (tagIndicatesMedal(tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tagIndicatesMedal(SteamTagDto tag) {
        var internal = tag.getInternalName();
        if (internal != null && internal.toLowerCase().contains("medal")) {
            return true;
        }
        var localized = tag.getLocalizedTagName();
        if (localized != null && localized.toLowerCase().contains("medal")) {
            return true;
        }
        var cat = tag.getCategory();
        if (cat != null && "Type".equalsIgnoreCase(cat.trim())) {
            var name = tag.getLocalizedTagName();
            if (name != null && name.toLowerCase().contains("collectible")) {
                var n = name.toLowerCase();
                if (n.contains("medal") || n.contains("coin")) {
                    return true;
                }
            }
        }
        return false;
    }
}
