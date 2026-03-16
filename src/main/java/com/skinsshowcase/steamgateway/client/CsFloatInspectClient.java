package com.skinsshowcase.steamgateway.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsshowcase.steamgateway.config.CsFloatProperties;
import com.skinsshowcase.steamgateway.dto.CsFloatBulkRequestDto;
import com.skinsshowcase.steamgateway.dto.CsFloatItemInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиент к CSFloat Inspect API (https://github.com/csfloat/inspect).
 * Поддерживает GET / с параметрами s, a, d, m и POST /bulk по inspect-ссылкам.
 */
@Slf4j
@Component
public class CsFloatInspectClient {

    private static final TypeReference<Map<String, CsFloatItemInfoDto>> BULK_RESPONSE_TYPE =
            new TypeReference<>() { };

    private final WebClient webClient;
    private final CsFloatProperties properties;
    private final ObjectMapper objectMapper;

    public CsFloatInspectClient(@Qualifier("csFloatWebClient") WebClient webClient,
                                CsFloatProperties properties,
                                ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Запрос данных по списку inspect-ссылок: сначала POST /bulk (один запрос), при пустом — GET с s,a,d,m или url=.
     * Ответ — карта assetId (параметр "a" или itemid) → iteminfo.
     */
    public Mono<Map<String, CsFloatItemInfoDto>> bulkInspect(List<String> inspectLinks) {
        if (inspectLinks == null || inspectLinks.isEmpty()) {
            return Mono.just(Map.of());
        }
        return postBulk(inspectLinks)
                .flatMap(result -> result.isEmpty() ? bulkInspectByGetParams(inspectLinks) : Mono.just(result));
    }

    /**
     * Один запрос GET / с параметрами s, a, d, m. Ответ — iteminfo (float, paintseed и т.д.).
     */
    public Mono<CsFloatItemInfoDto> getByParams(InspectLinkParams params) {
        if (params == null || params.getA() == null || params.getD() == null) {
            return Mono.justOrEmpty(null);
        }
        var builder = UriComponentsBuilder.fromPath("/").queryParam("a", params.getA()).queryParam("d", params.getD());
        if (params.getS() != null && !params.getS().isBlank()) {
            builder.queryParam("s", params.getS());
        }
        if (params.getM() != null && !params.getM().isBlank()) {
            builder.queryParam("m", params.getM());
        }
        var uri = builder.build().toUriString();
        log.debug("CsFloat GET request: {}", uri);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseGetResponse)
                .doOnError(e -> log.warn("CsFloat GET request failed: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Один запрос GET / с параметром url= (полная inspect-ссылка). Для ссылок, не парсящихся в s,a,d,m.
     */
    public Mono<CsFloatItemInfoDto> getByUrl(String inspectLink) {
        if (inspectLink == null || inspectLink.isBlank()) {
            return Mono.justOrEmpty(null);
        }
        var uri = UriComponentsBuilder.fromPath("/").queryParam("url", inspectLink.trim()).build().toUriString();
        log.debug("CsFloat GET by url (length {})", inspectLink.length());
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseGetResponse)
                .doOnError(e -> log.warn("CsFloat GET by url failed: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<CsFloatItemInfoDto> parseGetResponse(String json) {
        if (json == null || json.isBlank()) {
            return Mono.justOrEmpty(null);
        }
        try {
            var root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) {
                return Mono.justOrEmpty(null);
            }
            if (root.has("error") && !root.get("error").isNull()) {
                log.debug("CsFloat API error: {} code={}", root.path("error").asText(""), root.path("code").asInt(0));
                return Mono.justOrEmpty(null);
            }
            JsonNode iteminfoNode = root.path("iteminfo");
            if (iteminfoNode != null && iteminfoNode.isObject()) {
                var info = objectMapper.treeToValue(iteminfoNode, CsFloatItemInfoDto.class);
                if (info != null && info.getError() == null) {
                    return Mono.just(info);
                }
            }
            if (root.has("floatvalue") || root.has("itemid") || root.has("a")) {
                var flat = objectMapper.treeToValue(root, CsFloatItemInfoDto.class);
                if (flat != null && flat.getError() == null) {
                    return Mono.just(flat);
                }
            }
        } catch (Exception e) {
            log.trace("CsFloat GET parse failed: {}", e.getMessage());
        }
        return Mono.justOrEmpty(null);
    }

    /**
     * Запрос по списку параметров s,a,d,m, извлечённых из ответа Steam (descriptions[].actions[].link).
     * Только GET /?s=&a=&d=&m=, без POST /bulk и без GET по url=.
     * Ключ карты — параметр "a" (asset id).
     */
    public Mono<Map<String, CsFloatItemInfoDto>> bulkInspectByParams(List<InspectLinkParams> paramsList) {
        if (paramsList == null || paramsList.isEmpty()) {
            return Mono.just(Map.of());
        }
        var result = new ConcurrentHashMap<String, CsFloatItemInfoDto>();
        var monos = new ArrayList<Mono<Void>>();
        for (var p : paramsList) {
            if (p.getA() != null && p.getD() != null) {
                monos.add(getByParams(p).doOnNext(info -> putIfValid(result, p.getA(), info)).then());
            }
        }
        if (monos.isEmpty()) {
            return Mono.just(Map.of());
        }
        return Mono.when(monos).thenReturn(Map.copyOf(result));
    }

    /**
     * Запрос по списку inspect-ссылок: GET с s,a,d,m для парсящихся ссылок, GET с url= для остальных.
     */
    public Mono<Map<String, CsFloatItemInfoDto>> bulkInspectByGetParams(List<String> inspectLinks) {
        if (inspectLinks == null || inspectLinks.isEmpty()) {
            return Mono.just(Map.of());
        }
        var result = new ConcurrentHashMap<String, CsFloatItemInfoDto>();
        var monos = new ArrayList<Mono<Void>>();
        for (var link : inspectLinks) {
            var p = InspectLinkParams.fromInspectLink(link);
            if (p != null) {
                monos.add(getByParams(p).doOnNext(info -> putIfValid(result, p.getA(), info)).then());
            } else {
                monos.add(getByUrl(link).doOnNext(info -> putIfValid(result, csFloatKey(info), info)).then());
            }
        }
        if (monos.isEmpty()) {
            return Mono.just(Map.of());
        }
        return Mono.when(monos).thenReturn(Map.copyOf(result));
    }

    private static void putIfValid(ConcurrentHashMap<String, CsFloatItemInfoDto> result, String key, CsFloatItemInfoDto info) {
        if (info == null || info.getError() != null || key == null || key.isBlank()) {
            return;
        }
        result.put(key, info);
    }

    private static String csFloatKey(CsFloatItemInfoDto info) {
        if (info == null) {
            return null;
        }
        var a = info.getA();
        if (a != null && !a.isBlank()) {
            return a;
        }
        return info.getItemId();
    }

    private Mono<Map<String, CsFloatItemInfoDto>> postBulk(List<String> links) {
        var bodyBuilder = CsFloatBulkRequestDto.builder()
                .links(links.stream()
                        .map(link -> CsFloatBulkRequestDto.CsFloatBulkLinkDto.builder().link(link).build())
                        .toList());
        if (properties.getBulkKey() != null && !properties.getBulkKey().isBlank()) {
            bodyBuilder.bulkKey(properties.getBulkKey());
        }
        var body = bodyBuilder.build();

        log.debug("CsFloat bulk request: {} links", links.size());

        return webClient.post()
                .uri("/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(bodyStr -> {
                                    log.warn("CsFloat bulk error status={} body={}", resp.statusCode(), bodyStr);
                                    return Mono.<Throwable>just(new WebClientResponseException(
                                            resp.statusCode().value(),
                                            resp.statusCode().toString(),
                                            resp.headers().asHttpHeaders(),
                                            bodyStr != null ? bodyStr.getBytes(StandardCharsets.UTF_8) : new byte[0],
                                            StandardCharsets.UTF_8));
                                }))
                .bodyToMono(String.class)
                .flatMap(this::parseBulkResponse)
                .doOnError(e -> log.warn("CsFloat bulk request failed: {}", e.getMessage()))
                .onErrorReturn(Map.of());
    }

    private Mono<Map<String, CsFloatItemInfoDto>> parseBulkResponse(String json) {
        try {
            var map = objectMapper.readValue(json, BULK_RESPONSE_TYPE);
            if (map == null) {
                return Mono.just(Map.of());
            }
            if (isRootLevelErrorResponse(map)) {
                log.warn("CsFloat bulk returned root-level error: {}", json);
                return Mono.just(Map.of());
            }
            log.debug("CsFloat bulk parsed {} items", map.size());
            return Mono.just(map);
        } catch (Exception e) {
            log.warn("Failed to parse CsFloat bulk response: {} (first 500 chars: {})",
                    e.getMessage(), json != null && json.length() > 500 ? json.substring(0, 500) + "..." : json);
            return Mono.just(Map.of());
        }
    }

    private static boolean isRootLevelErrorResponse(Map<String, CsFloatItemInfoDto> map) {
        if (map.size() != 1 && map.size() != 2) {
            return false;
        }
        var hasError = map.containsKey("error");
        var hasCode = map.containsKey("code");
        return hasError || hasCode;
    }

    private static Map<String, CsFloatItemInfoDto> mergeBulkResults(Object[] results) {
        var merged = new LinkedHashMap<String, CsFloatItemInfoDto>();
        for (var r : results) {
            @SuppressWarnings("unchecked")
            var map = (Map<String, CsFloatItemInfoDto>) r;
            if (map != null) {
                merged.putAll(map);
            }
        }
        return merged;
    }

    private static List<List<String>> chunkList(List<String> list, int size) {
        var chunks = new ArrayList<List<String>>();
        for (var i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }
}
