package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Один предмет из инвентаря пользователя с опциональной ценой из каталога items (БД items).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Предмет инвентаря с данными Steam и (если есть) ценой из сервиса items")
public class InventoryItemDetailResponseDto {

    @Schema(description = "SteamID64 владельца")
    private String steamId;

    @Schema(description = "Отображаемое имя владельца: display_name из auth (или persona_name в БД), иначе GetPlayerSummaries; null, если недоступно")
    private String personaName;

    @Schema(description = "App ID инвентаря")
    private int appId;

    @Schema(description = "Context ID инвентаря")
    private int contextId;

    @Schema(description = "Данные предмета из инвентаря (float, стикеры, название и т.д.)")
    private InventoryItemResponseDto item;

    @Schema(description = "Цена и название из каталога items (null, если classid нет в БД или не заполнен)")
    private ItemCatalogPriceDto catalogPrice;
}
