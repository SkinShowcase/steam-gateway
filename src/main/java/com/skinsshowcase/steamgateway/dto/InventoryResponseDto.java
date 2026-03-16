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

    @Schema(description = "Идентификатор приложения (например 730 для CS2)")
    private Integer appId;

    @Schema(description = "Контекст инвентаря")
    private Integer contextId;

    @Schema(description = "Список предметов")
    private List<InventoryItemResponseDto> items;
}
