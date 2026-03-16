package com.skinsshowcase.steamgateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsshowcase.steamgateway.dto.SteamInventoryResponseDto;
import com.skinsshowcase.steamgateway.dto.SteamMarketSearchResponseDto;
import com.skinsshowcase.steamgateway.exception.SteamApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Клиент к Steam Community Inventory API.
 * Изолирует все вызовы к Steam; retry/backoff и таймауты настраиваются в конфиге WebClient.
 */
@Slf4j
@Component
public class SteamClient {

    private final WebClient steamWebClient;
    private final String inventoryPathTemplate;
    private final String marketSearchPathTemplate;
    private final ObjectMapper objectMapper;

    public SteamClient(@Qualifier("steamWebClient") WebClient steamWebClient,
                       SteamClientProperties properties,
                       ObjectMapper objectMapper) {
        this.steamWebClient = steamWebClient;
        this.inventoryPathTemplate = properties.getInventoryPathTemplate();
        this.marketSearchPathTemplate = properties.getMarketSearchPathTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Запрос инвентаря по Steam ID (64-bit), appId и contextId.
     *
     * @param steamId  SteamID64 (например 76561198000000000)
     * @param appId    App ID (730 — CS2/CS:GO, 753 — Steam)
     * @param contextId Context ID (2 — CS:GO/CS2 инвентарь)
     * @return ответ Steam или ошибка
     */
    public Mono<SteamInventoryResponseDto> getInventory(String steamId, int appId, int contextId) {
        var path = buildInventoryPath(steamId, appId, contextId);
        log.debug("Requesting Steam inventory: path={}", path);

        var retry = Retry.backoff(2, Duration.ofSeconds(1))
                .filter(this::isRetryableInventoryError)
                .doBeforeRetry(s -> log.warn("Retrying Steam request after failure: {}", s.failure().getMessage()));

        return steamWebClient
                .get()
                .uri(path)
                .exchangeToMono(response -> handleInventoryResponse(response))
                .retryWhen(retry)
                .doOnNext(r -> logInventorySuccess(r, steamId, appId, contextId))
                .onErrorMap(WebClientResponseException.class, this::toSteamApiException)
                .onErrorMap(e -> e instanceof SteamApiException ? e : new SteamApiException("Steam request failed", e));
    }

    private boolean isRetryableInventoryError(Throwable e) {
        if (e instanceof WebClientResponseException wce) {
            return wce.getStatusCode().is5xxServerError();
        }
        return true;
    }

    private Mono<SteamInventoryResponseDto> handleInventoryResponse(
            org.springframework.web.reactive.function.client.ClientResponse response) {
        if (!response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(response.statusCode().toString())
                    .flatMap(body -> Mono.error(buildWebClientException(response, body)));
        }
        return readBodyFromDataBuffers(response.body(BodyExtractors.toDataBuffers()))
                .flatMap(this::parseInventoryBody);
    }

    private void logInventorySuccess(SteamInventoryResponseDto r, String steamId, int appId, int contextId) {
        if (Boolean.FALSE.equals(r.getSuccess())) {
            log.warn("Steam returned success=false for steamId={}, appId={}, contextId={}",
                    steamId, appId, contextId);
        }
    }

    private SteamApiException toSteamApiException(WebClientResponseException e) {
        log.warn("Steam API error: status={}", e.getStatusCode());
        return new SteamApiException("Steam API error: " + e.getStatusCode(), e);
    }

    private WebClientResponseException buildWebClientException(
            org.springframework.web.reactive.function.client.ClientResponse response, String body) {
        var status = response.statusCode();
        var bytes = body.getBytes(StandardCharsets.UTF_8);
        return new WebClientResponseException(
                status.value(), status.toString(),
                response.headers().asHttpHeaders(), bytes, StandardCharsets.UTF_8);
    }

    private Mono<String> readBodyFromDataBuffers(Flux<DataBuffer> body) {
        return DataBufferUtils.join(body)
                .map(this::bufferToString);
    }

    private String bufferToString(DataBuffer buffer) {
        var bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Mono<SteamInventoryResponseDto> parseInventoryBody(String body) {
        try {
            return Mono.just(objectMapper.readValue(body, SteamInventoryResponseDto.class));
        } catch (Exception ex) {
            log.warn("Steam response parse error: {}; body prefix: {}",
                    ex.getMessage(), body.length() > 500 ? body.substring(0, 500) + "..." : body);
            return Mono.error(new SteamApiException("Steam API response parse error: " + ex.getMessage(), ex));
        }
    }

    private String buildInventoryPath(String steamId, int appId, int contextId) {
        return inventoryPathTemplate
                .replace("{steamId}", steamId)
                .replace("{appId}", String.valueOf(appId))
                .replace("{contextId}", String.valueOf(contextId));
    }

    /**
     * Запрос к Steam Market search/render для CS2 (appid=730).
     * Возвращает одну страницу результатов с минимальными ценами в USD (currency=1).
     *
     * @param start начальный индекс (пагинация)
     * @param count количество записей на странице (обычно 100)
     * @return ответ Steam Market или ошибка
     */
    public Mono<SteamMarketSearchResponseDto> getMarketSearch(int start, int count) {
        var path = marketSearchPathTemplate
                .replace("{start}", String.valueOf(start))
                .replace("{count}", String.valueOf(count));
        log.debug("Requesting Steam Market search: path={}", path);

        var retry = Retry.backoff(3, Duration.ofSeconds(15))
                .maxBackoff(Duration.ofSeconds(60))
                .filter(this::isRetryableMarketError)
                .doBeforeRetry(s -> log.warn("Retrying Steam Market request after 429/5xx: {}", s.failure().getMessage()));

        return steamWebClient
                .get()
                .uri(path)
                .exchangeToMono(this::handleMarketSearchResponse)
                .retryWhen(retry)
                .doOnNext(r -> logMarketSearchSuccess(r, start, count))
                .onErrorMap(WebClientResponseException.class, this::toSteamMarketApiException)
                .onErrorMap(e -> e instanceof SteamApiException ? e : new SteamApiException("Steam Market request failed", e));
    }

    private boolean isRetryableMarketError(Throwable e) {
        if (e instanceof WebClientResponseException wce) {
            return wce.getStatusCode().is5xxServerError() || wce.getStatusCode().value() == 429;
        }
        return true;
    }

    private Mono<SteamMarketSearchResponseDto> handleMarketSearchResponse(
            org.springframework.web.reactive.function.client.ClientResponse response) {
        if (!response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(response.statusCode().toString())
                    .flatMap(body -> Mono.error(buildWebClientException(response, body)));
        }
        return readBodyFromDataBuffers(response.body(BodyExtractors.toDataBuffers()))
                .flatMap(this::parseMarketSearchBody);
    }

    private void logMarketSearchSuccess(SteamMarketSearchResponseDto r, int start, int count) {
        if (Boolean.FALSE.equals(r.getSuccess())) {
            log.warn("Steam Market returned success=false for start={}, count={}", start, count);
        }
    }

    private SteamApiException toSteamMarketApiException(WebClientResponseException e) {
        log.warn("Steam Market API error: status={}", e.getStatusCode());
        return new SteamApiException("Steam Market API error: " + e.getStatusCode(), e);
    }

    private Mono<SteamMarketSearchResponseDto> parseMarketSearchBody(String body) {
        try {
            return Mono.just(objectMapper.readValue(body, SteamMarketSearchResponseDto.class));
        } catch (Exception ex) {
            log.warn("Steam Market response parse error: {}; body prefix: {}",
                    ex.getMessage(), body.length() > 500 ? body.substring(0, 500) + "..." : body);
            return Mono.error(new SteamApiException(
                    "Steam Market API response parse error: " + ex.getMessage(), ex));
        }
    }
}
