package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST API: инвентарь по Steam ID.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Inventory", description = "Инвентарь Steam по Steam ID")
@RequiredArgsConstructor
public class InventoryController {

    /**
     * SteamID64: 17 цифр, обычно начинается с 765.
     */
    private static final String STEAM_ID64_PATTERN = "^765[0-9]{14}$";

    private final InventoryService inventoryService;

    @GetMapping(value = "/inventory/{steamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Получить инвентарь по Steam ID",
            description = "Возвращает список предметов в инвентаре пользователя Steam. " +
                    "По умолчанию запрашивается инвентарь CS2 (appId=730, contextId=2). " +
                    "Инвентарь должен быть публичным."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список предметов",
                    content = @Content(schema = @Schema(implementation = InventoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный Steam ID"),
            @ApiResponse(responseCode = "502", description = "Ошибка Steam API / недоступность инвентаря")
    })
    public Mono<InventoryResponseDto> getInventory(
            @Parameter(description = "Steam ID пользователя (SteamID64, 17 цифр)", required = true, example = "76561198000000000")
            @PathVariable @Pattern(regexp = STEAM_ID64_PATTERN, message = "Steam ID должен быть в формате SteamID64 (17 цифр, начинается с 765)") String steamId,
            @Parameter(description = "App ID (730 — CS2/CS:GO, 753 — Steam)")
            @RequestParam(defaultValue = "730") @Min(1) @Max(999999) int appId,
            @Parameter(description = "Context ID (2 — инвентарь CS2)")
            @RequestParam(defaultValue = "2") @Min(0) @Max(999) int contextId
    ) {
        return inventoryService.getInventory(steamId, appId, contextId);
    }
}
