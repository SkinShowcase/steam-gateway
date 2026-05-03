package com.skinsshowcase.steamgateway.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StickerParserTest {

    @Test
    void parseFromStickerInfo_nullOrBlank_returnsNull() {
        assertThat(StickerParser.parseFromStickerInfo(null)).isNull();
        assertThat(StickerParser.parseFromStickerInfo("   ")).isNull();
    }

    @Test
    void parseFromStickerInfo_extractsTitleAttributes() {
        var html = "<div title=\"Sticker: Team Liquid\"></div><span title='Sticker: Holo | Katowice 2019'></span>";

        var list = StickerParser.parseFromStickerInfo(html);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getSlot()).isZero();
        assertThat(list.get(0).getName()).isEqualTo("Team Liquid");
        assertThat(list.get(1).getName()).isEqualTo("Holo | Katowice 2019");
    }

    @Test
    void parseFromStickerInfo_fallsBackToTrailingText() {
        var text = "<div>foo</div>Sticker: One, Two, <b>Three</b>";

        var list = StickerParser.parseFromStickerInfo(text);

        assertThat(list).hasSize(3);
        assertThat(list.get(0).getName()).isEqualTo("One");
        assertThat(list.get(2).getName()).isEqualTo("Three");
    }

    @Test
    void tryParse_nullOrNonJson_returnsNull() {
        assertThat(StickerParser.tryParse(null)).isNull();
        assertThat(StickerParser.tryParse("not json")).isNull();
    }

    @Test
    void tryParse_jsonArray_mapsFields() {
        var json = "[{\"slot\":0,\"sticker_id\":123,\"name\":\"X\",\"wear\":0.5}]";

        var list = StickerParser.tryParse(json);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getSlot()).isZero();
        assertThat(list.get(0).getStickerId()).isEqualTo(123L);
        assertThat(list.get(0).getName()).isEqualTo("X");
        assertThat(list.get(0).getWear()).isEqualTo(0.5);
    }
}
