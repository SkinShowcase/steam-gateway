package com.skinsshowcase.steamgateway.client;

import com.skinsshowcase.steamgateway.config.AuthServiceProperties;
import com.skinsshowcase.steamgateway.dto.auth.AuthProfileLabelsRequestDto;
import com.skinsshowcase.steamgateway.dto.auth.AuthProfileLabelsResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Внутренний API auth: отображаемое имя пользователя (display_name / persona_name в БД).
 */
@Slf4j
@Component
public class AuthProfileLabelsClient {

    private static final String INTERNAL_KEY_HEADER = "X-Internal-Service-Key";
    private static final String PROFILE_LABELS_PATH = "/auth/internal/users/profile-labels";

    private final WebClient webClient;
    private final AuthServiceProperties properties;

    public AuthProfileLabelsClient(@Qualifier("authServiceWebClient") WebClient webClient,
                                   AuthServiceProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public Mono<Map<String, String>> fetchLabels(List<String> steamIds) {
        if (steamIds == null || steamIds.isEmpty()) {
            return Mono.just(Collections.emptyMap());
        }
        var body = new AuthProfileLabelsRequestDto(steamIds);
        var spec = webClient.post()
                .uri(PROFILE_LABELS_PATH)
                .contentType(MediaType.APPLICATION_JSON);
        spec = withInternalKeyHeader(spec);
        return spec.bodyValue(body)
                .retrieve()
                .bodyToMono(AuthProfileLabelsResponseDto.class)
                .map(AuthProfileLabelsClient::labelsOrEmpty)
                .onErrorResume(e -> {
                    log.warn("Auth profile-labels failed: {}", e.getMessage());
                    return Mono.just(Collections.emptyMap());
                });
    }

    private WebClient.RequestBodySpec withInternalKeyHeader(WebClient.RequestBodySpec spec) {
        var key = properties.getInternalServiceKey();
        if (!StringUtils.hasText(key)) {
            return spec;
        }
        return spec.header(INTERNAL_KEY_HEADER, key);
    }

    private static Map<String, String> labelsOrEmpty(AuthProfileLabelsResponseDto dto) {
        if (dto == null || dto.labelBySteamId() == null) {
            return Collections.emptyMap();
        }
        return dto.labelBySteamId();
    }
}
