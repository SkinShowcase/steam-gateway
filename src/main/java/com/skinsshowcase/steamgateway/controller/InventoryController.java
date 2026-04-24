package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.dto.InventoryItemDetailResponseDto;
import com.skinsshowcase.steamgateway.dto.InventoryResponseDto;
import com.skinsshowcase.steamgateway.service.InventoryItemDetailService;
import com.skinsshowcase.steamgateway.service.InventoryService;
import com.skinsshowcase.steamgateway.service.OwnerDisplayLabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    private final InventoryItemDetailService inventoryItemDetailService;
    private final OwnerDisplayLabelService ownerDisplayLabelService;

    @GetMapping(value = "/inventory/{steamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Получить инвентарь по Steam ID",
            description = "Возвращает список предметов в инвентаре пользователя Steam. " +
                    "Только tradable-предметы; медали и аналогичные collectible исключаются. " +
                    "Поле catalogMinPriceUsd подтягивается из каталога items (null, если нет цены). " +
                    "Поле personaName — отображаемое имя из сервиса auth (display_name, иначе persona_name в БД), при отсутствии — из GetPlayerSummaries; не кешируется вместе со списком предметов. " +
                    "По умолчанию appId=730, contextId=2. Инвентарь должен быть публичным. Список предметов кешируется до 1 ч."
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
        return Mono.zip(
                inventoryService.getInventory(steamId, appId, contextId),
                ownerDisplayLabelService.resolveOwnerLabelHolderMono(steamId)
        ).map(t -> InventoryController.withPersonaName(t.getT1(), t.getT2().orElse(null)));
    }

    private static InventoryResponseDto withPersonaName(InventoryResponseDto inv, String personaName) {
        return InventoryResponseDto.builder()
                .steamId(inv.getSteamId())
                .personaName(personaName)
                .appId(inv.getAppId())
                .contextId(inv.getContextId())
                .items(inv.getItems())
                .build();
    }

    @GetMapping(value = "/inventory/{steamId}/item", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Один предмет инвентаря с ценой из каталога",
            description = "Идентификация как при сохранении набора для обмена: query-параметры assetId (Steam asset id) и classId (Steam classid / item_id в каталоге items). " +
                    "Сначала загружается инвентарь Steam; если пары нет в ответе — 404. " +
                    "Цена (minPriceUsd) подтягивается из сервиса items по classId. catalogPrice будет null, если предмета нет в каталоге, цена не заполнена, либо сервис items недоступен. " +
                    "Поле personaName — отображаемое имя из auth (display_name / persona_name в БД), иначе из GetPlayerSummaries; null, если имя недоступно ни из одного источника."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Предмет найден",
                    content = @Content(schema = @Schema(implementation = InventoryItemDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден в инвентаре"),
            @ApiResponse(responseCode = "502", description = "Ошибка Steam API")
    })
    public Mono<InventoryItemDetailResponseDto> getInventoryItemDetail(
            @Parameter(description = "Steam ID пользователя (SteamID64)", required = true, example = "76561198000000000")
            @PathVariable @Pattern(regexp = STEAM_ID64_PATTERN, message = "Steam ID должен быть в формате SteamID64 (17 цифр, начинается с 765)") String steamId,
            @Parameter(description = "Steam asset id", required = true, example = "12345678901")
            @RequestParam @NotBlank(message = "assetId обязателен") String assetId,
            @Parameter(description = "Steam classid (как в теле trades: classId)", required = true, example = "310776785")
            @RequestParam @NotBlank(message = "classId обязателен") String classId,
            @Parameter(description = "App ID (730 — CS2)")
            @RequestParam(defaultValue = "730") @Min(1) @Max(999999) int appId,
            @Parameter(description = "Context ID (2 — инвентарь CS2)")
            @RequestParam(defaultValue = "2") @Min(0) @Max(999) int contextId
    ) {
        return inventoryItemDetailService.getItemDetail(steamId, assetId, classId, appId, contextId);
    }
}
