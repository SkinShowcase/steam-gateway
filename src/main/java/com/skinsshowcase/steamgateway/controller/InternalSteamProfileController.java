package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.client.SteamWebApiClient;
import com.skinsshowcase.steamgateway.dto.SteamProfileResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Сводка профиля Steam для вызовов из auth (не публикуется через API Gateway).
 */
@RestController
@RequestMapping(path = "/internal/v1/steam", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Hidden
@RequiredArgsConstructor
public class InternalSteamProfileController {

    private static final String STEAM_ID64_PATTERN = "^765[0-9]{14}$";

    private final SteamWebApiClient steamWebApiClient;

    @GetMapping("/profile/{steamId}")
    public Mono<ResponseEntity<SteamProfileResponseDto>> getProfile(@PathVariable String steamId) {
        if (!steamId.matches(STEAM_ID64_PATTERN)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return steamWebApiClient.getPlayerProfile(steamId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
