package com.skinsshowcase.steamgateway.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspectLinkParamsTest {

    @Test
    void fromInspectLink_nullOrBlank_returnsNull() {
        assertThat(InspectLinkParams.fromInspectLink(null)).isNull();
        assertThat(InspectLinkParams.fromInspectLink("  ")).isNull();
    }

    @Test
    void fromInspectLink_inventoryStyle() {
        var link = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20S76561198084749846A698323590D7935523998312483177";

        var p = InspectLinkParams.fromInspectLink(link);

        assertThat(p).isNotNull();
        assertThat(p.getS()).isEqualTo("76561198084749846");
        assertThat(p.getA()).isEqualTo("698323590");
        assertThat(p.getD()).isEqualTo("7935523998312483177");
        assertThat(p.getM()).isNull();
    }

    @Test
    void fromInspectLink_marketStyle() {
        var raw = "M625254122282020305A6760346663D30614827701953021";

        var p = InspectLinkParams.fromInspectLink(raw);

        assertThat(p).isNotNull();
        assertThat(p.getS()).isNull();
        assertThat(p.getM()).isEqualTo("625254122282020305");
        assertThat(p.getA()).isEqualTo("6760346663");
        assertThat(p.getD()).isEqualTo("30614827701953021");
    }

    @Test
    void fromInspectLink_unrecognized_returnsNull() {
        assertThat(InspectLinkParams.fromInspectLink("https://example.com/nothing")).isNull();
    }
}
