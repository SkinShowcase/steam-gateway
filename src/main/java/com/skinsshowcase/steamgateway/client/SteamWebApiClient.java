package com.skinsshowcase.steamgateway.client;

import com.skinsshowcase.steamgateway.config.SteamWebApiProperties;
import com.skinsshowcase.steamgateway.dto.SteamProfileResponseDto;
import com.skinsshowcase.steamgateway.metrics.SteamGatewayMetrics;
import com.skinsshowcase.steamgateway.dto.steamweb.SteamPlayerSummariesEnvelopeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Steam Web API: ISteamUser/GetPlayerSummaries.
 */
@Slf4j
@Component
public class SteamWebApiClient {

    private static final String PATH = "/ISteamUser/GetPlayerSummaries/v2/";

    private final WebClient steamWebApiWebClient;
    private final SteamWebApiProperties steamWebApiProperties;
    private final SteamGatewayMetrics steamGatewayMetrics;

    public SteamWebApiClient(@Qualifier("steamWebApiWebClient") WebClient steamWebApiWebClient,
                             SteamWebApiProperties steamWebApiProperties,
                             SteamGatewayMetrics steamGatewayMetrics) {
        this.steamWebApiWebClient = steamWebApiWebClient;
        this.steamWebApiProperties = steamWebApiProperties;
        this.steamGatewayMetrics = steamGatewayMetrics;
    }

    public Mono<SteamProfileResponseDto> getPlayerProfile(String steamId64) {
        var key = steamWebApiProperties.getKey();
        if (!StringUtils.hasText(key)) {
            log.warn("STEAM_API_KEY is not set; cannot load Steam profile");
            return Mono.empty();
        }
        var mono = steamWebApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("key", key)
                        .queryParam("steamids", steamId64)
                        .build())
                .retrieve()
                .bodyToMono(SteamPlayerSummariesEnvelopeDto.class)
                .flatMap(envelope -> toProfileMono(envelope, steamId64))
                .onErrorResume(e -> {
                    log.warn("GetPlayerSummaries failed: {}", e.getClass().getSimpleName());
                    return Mono.empty();
                });
        return steamGatewayMetrics.traceOutbound(mono, "steam_web_api", "get_player_summaries");
    }

    private static Mono<SteamProfileResponseDto> toProfileMono(SteamPlayerSummariesEnvelopeDto envelope, String steamId64) {
        var profile = mapFirstPlayer(envelope, steamId64);
        if (profile == null) {
            return Mono.empty();
        }
        return Mono.just(profile);
    }

    private static SteamProfileResponseDto mapFirstPlayer(SteamPlayerSummariesEnvelopeDto envelope, String requestedSteamId) {
        if (envelope == null || envelope.getResponse() == null) {
            return null;
        }
        var players = envelope.getResponse().getPlayers();
        if (players == null || players.isEmpty()) {
            return null;
        }
        var p = players.get(0);
        if (p == null) {
            return null;
        }
        var sid = p.getSteamId() != null ? p.getSteamId() : requestedSteamId;
        return SteamProfileResponseDto.builder()
                .steamId(sid)
                .personaName(p.getPersonaName())
                .avatarUrl(p.getAvatarFull())
                .avatarMediumUrl(p.getAvatarMedium())
                .build();
    }
}
