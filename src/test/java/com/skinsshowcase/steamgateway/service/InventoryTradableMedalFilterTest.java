package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.dto.SteamDescriptionDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTradableMedalFilterTest {

    @Test
    void excludesNullDescription() {
        assertFalse(InventoryTradableMedalFilter.isInventoryItemIncluded(null));
    }

    @Test
    void excludesNonTradable() {
        var d = new SteamDescriptionDto();
        d.setTradable(0);
        d.setType("Rifle");
        assertFalse(InventoryTradableMedalFilter.isInventoryItemIncluded(d));
    }

    @Test
    void includesTradableSkin() {
        var d = new SteamDescriptionDto();
        d.setTradable(1);
        d.setType("Rifle");
        d.setName("AK-47 | Redline (Field-Tested)");
        assertTrue(InventoryTradableMedalFilter.isInventoryItemIncluded(d));
    }

    @Test
    void excludesServiceMedal() {
        var d = new SteamDescriptionDto();
        d.setTradable(1);
        d.setType("Extraordinary");
        d.setName("Service Medal 2023");
        assertFalse(InventoryTradableMedalFilter.isInventoryItemIncluded(d));
    }
}
