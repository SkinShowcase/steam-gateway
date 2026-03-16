package com.skinsshowcase.steamgateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsshowcase.steamgateway.config.CsFloatProperties;
import com.skinsshowcase.steamgateway.dto.CsFloatItemInfoDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка, что при ответе CSFloat с iteminfo (в т.ч. floatvalue) мы получаем не-null float.
 */
class CsFloatInspectClientTest {

    private MockWebServer mockServer;
    private CsFloatInspectClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        var baseUrl = mockServer.url("/").toString().replaceAll("/$", "");
        var props = new CsFloatProperties();
        props.setBaseUrl(baseUrl);
        props.setEnabled(true);
        var webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        client = new CsFloatInspectClient(webClient, props, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void getByParams_returnsFloatWhenApiReturnsIteminfo() throws Exception {
        var json = "{\"iteminfo\":{\"itemid\":\"13874827217\",\"a\":\"698323590\",\"defindex\":7,\"paintindex\":282,\"paintseed\":361,\"floatvalue\":0.22740158438682556,\"wear_name\":\"Field-Tested\",\"full_item_name\":\"AK-47 | Redline (Field-Tested)\"}}";
        mockServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        var params = InspectLinkParams.builder()
                .s("76561198084749846")
                .a("698323590")
                .d("7935523998312483177")
                .m(null)
                .build();

        var result = client.getByParams(params).block();

        assertThat(result).as("getByParams must return iteminfo when mock returns 200 with iteminfo").isNotNull();
        assertThat(result.getFloatValue()).isNotNull();
        assertThat(result.getFloatValue()).isEqualTo(0.22740158438682556);
        assertThat(result.getPaintSeed()).isEqualTo(361);
        assertThat(result.getWearName()).isEqualTo("Field-Tested");
    }

    @Test
    void bulkInspect_withLegacyLink_returnsMapWithNonNullFloat() throws Exception {
        mockServer.enqueue(new MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(200));
        var json = "{\"iteminfo\":{\"itemid\":\"13874827217\",\"a\":\"698323590\",\"floatvalue\":0.15,\"paintseed\":100}}";
        mockServer.enqueue(new MockResponse().setBody(json).addHeader("Content-Type", "application/json").setResponseCode(200));

        var legacyLink = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20S76561198084749846A698323590D7935523998312483177";
        var result = client.bulkInspect(List.of(legacyLink)).block();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        var info = result.get("698323590");
        assertThat(info).as("bulkInspect must return map with assetId key when GET returns iteminfo").isNotNull();
        assertThat(info.getFloatValue()).isNotNull();
        assertThat(info.getFloatValue()).isEqualTo(0.15);
    }

    @Test
    void bulkInspect_fallbackToGetWhenBulkReturnsEmpty() throws Exception {
        mockServer.enqueue(new MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(200));
        var getJson = "{\"iteminfo\":{\"a\":\"698323590\",\"floatvalue\":0.2,\"paintseed\":50}}";
        mockServer.enqueue(new MockResponse().setBody(getJson).addHeader("Content-Type", "application/json").setResponseCode(200));

        var legacyLink = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20S76561198084749846A698323590D7935523998312483177";
        var result = client.bulkInspect(List.of(legacyLink)).block();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        var info = result.get("698323590");
        assertThat(info).isNotNull();
        assertThat(info.getFloatValue()).isNotNull();
        assertThat(info.getFloatValue()).isEqualTo(0.2);
    }

    @Test
    void bulkInspect_bulkReturnsNonEmptyMap() throws Exception {
        var bulkJson = "{\"698323590\":{\"a\":\"698323590\",\"floatvalue\":0.33,\"paintseed\":77}}";
        mockServer.enqueue(new MockResponse().setBody(bulkJson).addHeader("Content-Type", "application/json").setResponseCode(200));

        var legacyLink = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20S76561198084749846A698323590D7935523998312483177";
        var result = client.bulkInspect(List.of(legacyLink)).block();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        var info = result.get("698323590");
        assertThat(info).isNotNull();
        assertThat(info.getFloatValue()).isNotNull();
        assertThat(info.getFloatValue()).isEqualTo(0.33);
    }
}
