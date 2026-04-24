package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ответ API: список предметов в инвентаре по Steam ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Список предметов в инвентаре Steam")
public class InventoryResponseDto {

    @Schema(description = "Steam ID пользователя (64-bit)")
    private String steamId;

    @Schema(description = "Отображаемое имя владельца: display_name из auth (или persona_name в БД), иначе ник из Steam Web API при успешном запросе; null если ни один источник не дал имя")
    private String personaName;

    @Schema(description = "Идентификатор приложения (например 730 для CS2)")
    private Integer appId;

    @Schema(description = "Контекст инвентаря")
    private Integer contextId;

    @Schema(description = "Список предметов")
    private List<InventoryItemResponseDto> items;
}
