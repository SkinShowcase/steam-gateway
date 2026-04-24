package com.skinsshowcase.steamgateway.client;

import com.skinsshowcase.steamgateway.dto.ItemCatalogPriceDto;
import com.skinsshowcase.steamgateway.dto.ItemsPriceBatchRequestDto;
import com.skinsshowcase.steamgateway.dto.ItemsPriceBatchResponseDto;
import com.skinsshowcase.steamgateway.metrics.SteamGatewayMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клиент к сервису items: цена и имя по Steam classid (item_id в каталоге).
 */
@Slf4j
@Component
public class ItemsCatalogClient {

    private final WebClient webClient;
    private final SteamGatewayMetrics steamGatewayMetrics;

    public ItemsCatalogClient(@Qualifier("itemsCatalogWebClient") WebClient itemsCatalogWebClient,
                              SteamGatewayMetrics steamGatewayMetrics) {
        this.webClient = itemsCatalogWebClient;
        this.steamGatewayMetrics = steamGatewayMetrics;
    }

    /**
     * @return пустой Mono, если предмета нет (404), цены нет (minPriceUsd null), каталог недоступен или вернул ошибку
     */
    public Mono<ItemCatalogPriceDto> fetchCatalogPriceByClassId(String classId) {
        var mono = webClient.get()
                .uri("/api/v1/items/{itemId}", classId)
                .exchangeToMono(this::mapCatalogResponse)
                .onErrorResume(this::catalogTransportFailureEmpty);
        return steamGatewayMetrics.traceOutbound(mono, "items_catalog", "get_item");
    }

    /**
     * Пакет запрос цен (POST /api/v1/items/prices). При ошибке — пустая карта.
     */
    public Mono<Map<String, ItemCatalogPriceDto>> fetchCatalogPricesBatch(List<String> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Mono.just(Map.of());
        }
        var body = new ItemsPriceBatchRequestDto(new ArrayList<>(classIds));
        var mono = webClient.post()
                .uri("/api/v1/items/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(this::mapBatchResponse)
                .onErrorResume(this::batchTransportFailureEmpty);
        return steamGatewayMetrics.traceOutbound(mono, "items_catalog", "prices_batch");
    }

    private Mono<Map<String, ItemCatalogPriceDto>> mapBatchResponse(ClientResponse response) {
        var status = response.statusCode();
        if (!status.is2xxSuccessful()) {
            log.warn("Items catalog batch returned status {}, omitting prices", status.value());
            return response.releaseBody().then(Mono.just(Map.of()));
        }
        return response.bodyToMono(ItemsPriceBatchResponseDto.class).map(this::extractPricesOrEmpty);
    }

    private Map<String, ItemCatalogPriceDto> extractPricesOrEmpty(ItemsPriceBatchResponseDto dto) {
        if (dto == null || dto.getPrices() == null) {
            return Map.of();
        }
        return new LinkedHashMap<>(dto.getPrices());
    }

    private Mono<Map<String, ItemCatalogPriceDto>> batchTransportFailureEmpty(Throwable error) {
        log.warn("Items catalog batch request failed: {}", error.getClass().getSimpleName());
        return Mono.just(Map.of());
    }

    private Mono<ItemCatalogPriceDto> mapCatalogResponse(ClientResponse response) {
        var status = response.statusCode();
        if (status.value() == 404) {
            return response.releaseBody().then(Mono.empty());
        }
        if (status.is2xxSuccessful()) {
            return response.bodyToMono(ItemCatalogPriceDto.class).flatMap(this::monoIfHasPrice);
        }
        return catalogHttpFailureEmpty(response, status);
    }

    private Mono<ItemCatalogPriceDto> monoIfHasPrice(ItemCatalogPriceDto dto) {
        if (dto.getMinPriceUsd() == null) {
            return Mono.empty();
        }
        return Mono.just(dto);
    }

    private Mono<ItemCatalogPriceDto> catalogHttpFailureEmpty(ClientResponse response, HttpStatusCode status) {
        log.warn("Items catalog returned non-success status, omitting catalog price: {}", status.value());
        return response.releaseBody().then(Mono.empty());
    }

    private Mono<ItemCatalogPriceDto> catalogTransportFailureEmpty(Throwable error) {
        log.warn("Items catalog request failed, omitting catalog price: {}", error.getClass().getSimpleName());
        return Mono.empty();
    }
}
