package com.skinsshowcase.steamgateway.client;

import com.skinsshowcase.steamgateway.config.LisSkinsProperties;
import com.skinsshowcase.steamgateway.dto.LisSkinsExportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Клиент к lis-skins.com: загрузка экспорта цен CS2 (api_csgo_full.json).
 */
@Slf4j
@Component
public class LisSkinsClient {

    private final WebClient webClient;
    private final LisSkinsProperties properties;

    public LisSkinsClient(@Qualifier("lisSkinsWebClient") WebClient webClient,
                          LisSkinsProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /**
     * Загружает полный экспорт CS2 (items с name и price в USD).
     * Retry при 5xx и таймаутах.
     */
    public Mono<LisSkinsExportDto> getFullExport() {
        var url = properties.getExportUrl();
        log.debug("Requesting lis-skins export: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(LisSkinsExportDto.class)
                .retryWhen(Retry.backoff(properties.getMaxRetries(), Duration.ofSeconds(2))
                        .filter(LisSkinsClient::isRetryable)
                        .doBeforeRetry(s -> log.warn("Retrying lis-skins request after error: {}", s.failure().getMessage())))
                .doOnSuccess(this::logExportSize);
    }

    private void logExportSize(LisSkinsExportDto dto) {
        var size = dto != null && dto.getItems() != null ? dto.getItems().size() : 0;
        log.debug("Lis-skins export loaded: {} items", size);
    }

    private static boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException e) {
            int code = e.getStatusCode().value();
            return code >= 500 || code == 429;
        }
        return t instanceof java.net.ConnectException
                || t instanceof java.net.SocketTimeoutException
                || t instanceof org.springframework.web.reactive.function.client.WebClientException;
    }
}
