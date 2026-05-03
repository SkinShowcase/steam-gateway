package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.dto.SteamDescriptionDto;
import com.skinsshowcase.steamgateway.dto.SteamTagDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryTradableMedalFilterTest {

    @Test
    void nullDescription_excluded() {
        assertThat(InventoryTradableMedalFilter.isInventoryItemIncluded(null)).isFalse();
    }

    @Test
    void notTradable_excluded() {
        var d = new SteamDescriptionDto();
        d.setTradable(0);
        assertThat(InventoryTradableMedalFilter.isInventoryItemIncluded(d)).isFalse();
    }

    @Test
    void tradableRifle_included() {
        var d = new SteamDescriptionDto();
        d.setTradable(1);
        d.setType("Rifle");
        d.setName("AK-47 | Redline");
        assertThat(InventoryTradableMedalFilter.isInventoryItemIncluded(d)).isTrue();
    }

    @Test
    void tradableServiceMedal_excluded() {
        var d = new SteamDescriptionDto();
        d.setTradable(1);
        d.setType("Collectible — Service Medal 2024");
        assertThat(InventoryTradableMedalFilter.isInventoryItemIncluded(d)).isFalse();
    }

    @Test
    void tradableMedalViaTag_excluded() {
        var d = new SteamDescriptionDto();
        d.setTradable(1);
        d.setName("Something");
        var tag = new SteamTagDto();
        tag.setInternalName("collectible_medal_2020");
        d.setTags(List.of(tag));
        assertThat(InventoryTradableMedalFilter.isInventoryItemIncluded(d)).isFalse();
    }
}
