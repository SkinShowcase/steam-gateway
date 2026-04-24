package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.client.AuthProfileLabelsClient;
import com.skinsshowcase.steamgateway.client.SteamWebApiClient;
import com.skinsshowcase.steamgateway.dto.SteamProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Имя владельца для API: сначала auth (display_name, иначе persona_name в БД), затем Steam GetPlayerSummaries.
 */
@Service
@RequiredArgsConstructor
public class OwnerDisplayLabelService {

    private final AuthProfileLabelsClient authProfileLabelsClient;
    private final SteamWebApiClient steamWebApiClient;

    public Mono<Optional<String>> resolveOwnerLabelHolderMono(String steamId) {
        return authProfileLabelsClient.fetchLabels(List.of(steamId))
                .flatMap(map -> resolveAfterAuthMap(map, steamId));
    }

    private Mono<Optional<String>> resolveAfterAuthMap(Map<String, String> map, String steamId) {
        var fromAuth = optionalLabelFromMap(map, steamId);
        if (fromAuth.isPresent()) {
            return Mono.just(fromAuth);
        }
        return steamWebApiClient.getPlayerProfile(steamId)
                .map(OwnerDisplayLabelService::optionalSteamPersona)
                .defaultIfEmpty(Optional.empty());
    }

    private static Optional<String> optionalLabelFromMap(Map<String, String> map, String steamId) {
        if (map == null || steamId == null) {
            return Optional.empty();
        }
        var v = map.get(steamId.trim());
        if (v == null || v.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(v.trim());
    }

    private static Optional<String> optionalSteamPersona(SteamProfileResponseDto profile) {
        if (profile == null || profile.getPersonaName() == null) {
            return Optional.empty();
        }
        var n = profile.getPersonaName().trim();
        if (n.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(n);
    }
}
