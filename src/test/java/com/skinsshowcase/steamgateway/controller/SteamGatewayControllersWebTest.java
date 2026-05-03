package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.client.SteamWebApiClient;
import com.skinsshowcase.steamgateway.config.SteamGatewayInternalProperties;
import com.skinsshowcase.steamgateway.dto.InventoryItemDetailResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.LisSkinsExportDto;
import com.skinsshowcase.steamgateway.dto.SteamProfileResponseDto;
import com.skinsshowcase.steamgateway.service.InventoryItemDetailService;
import com.skinsshowcase.steamgateway.service.InventoryService;
import com.skinsshowcase.steamgateway.service.LisSkinsExportService;
import com.skinsshowcase.steamgateway.service.OwnerDisplayLabelService;
import com.skinsshowcase.steamgateway.service.SteamGatewayCacheSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SteamGatewayControllersWebTest {

    private static final String SID = "76561198000000001";

    @Test
    void inventory_get_ok() {
        var invSvc = mock(InventoryService.class);
        var detailSvc = mock(InventoryItemDetailService.class);
        var ownerSvc = mock(OwnerDisplayLabelService.class);
        var base = InventoryResponseDto.builder()
                .steamId(SID)
                .appId(730)
                .contextId(2)
                .items(List.of())
                .build();
        when(invSvc.getInventory(SID, 730, 2)).thenReturn(Mono.just(base));
        when(ownerSvc.resolveOwnerLabelHolderMono(SID)).thenReturn(Mono.just(Optional.of("Nick")));

        var client = WebTestClient.bindToController(new InventoryController(invSvc, detailSvc, ownerSvc)).build();

        client.get().uri("/api/v1/inventory/{sid}", SID).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.personaName").isEqualTo("Nick");
    }

    @Test
    void inventoryItemDetail_ok() {
        var invSvc = mock(InventoryService.class);
        var detailSvc = mock(InventoryItemDetailService.class);
        var ownerSvc = mock(OwnerDisplayLabelService.class);
        var body = InventoryItemDetailResponseDto.builder()
                .steamId(SID)
                .appId(730)
                .contextId(2)
                .build();
        when(detailSvc.getItemDetail(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Mono.just(body));

        var client = WebTestClient.bindToController(new InventoryController(invSvc, detailSvc, ownerSvc)).build();

        client.get().uri(uriBuilder -> uriBuilder.path("/api/v1/inventory/{sid}/item")
                        .queryParam("assetId", "1")
                        .queryParam("classId", "2")
                        .build(SID))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void internalProfile_badSteamId_400() {
        var api = mock(SteamWebApiClient.class);
        var client = WebTestClient.bindToController(new InternalSteamProfileController(api)).build();

        client.get().uri("/internal/v1/steam/profile/{sid}", "bad").exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void internalProfile_notFound() {
        var api = mock(SteamWebApiClient.class);
        when(api.getPlayerProfile(SID)).thenReturn(Mono.empty());
        var client = WebTestClient.bindToController(new InternalSteamProfileController(api)).build();

        client.get().uri("/internal/v1/steam/profile/{sid}", SID).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void internalProfile_ok() {
        var api = mock(SteamWebApiClient.class);
        var profile = SteamProfileResponseDto.builder()
                .steamId(SID)
                .personaName("p")
                .avatarUrl("a")
                .avatarMediumUrl("m")
                .build();
        when(api.getPlayerProfile(SID)).thenReturn(Mono.just(profile));
        var client = WebTestClient.bindToController(new InternalSteamProfileController(api)).build();

        client.get().uri("/internal/v1/steam/profile/{sid}", SID).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.personaName").isEqualTo("p");
    }

    @Test
    void cacheEvict_forbiddenAndNoContent() {
        var props = mock(SteamGatewayInternalProperties.class);
        var cache = mock(SteamGatewayCacheSupport.class);
        when(props.getCacheEvictToken()).thenReturn("tok");
        var client = WebTestClient.bindToController(new InternalCacheEvictController(props, cache)).build();

        client.post().uri("/internal/v1/cache/evict").exchange().expectStatus().isForbidden();

        client.post().uri("/internal/v1/cache/evict").header("X-Internal-Token", "tok").exchange()
                .expectStatus().isNoContent();
        verify(cache).evictAllSteamGatewayCaches();
    }

    @Test
    void lisSkinsExport_502_and_200() {
        var svc = mock(LisSkinsExportService.class);
        when(svc.getFullExport()).thenReturn(null);
        var client = WebTestClient.bindToController(new LisSkinsExportController(svc)).build();
        client.get().uri("/api/v1/market/cs2/export").exchange().expectStatus().isEqualTo(502);

        when(svc.getFullExport()).thenReturn(new LisSkinsExportDto());
        client = WebTestClient.bindToController(new LisSkinsExportController(svc)).build();
        client.get().uri("/api/v1/market/cs2/export").exchange().expectStatus().isOk();
    }

    @Test
    void marketController_isLoadable() {
        assertThat(new MarketController()).isNotNull();
    }
}
