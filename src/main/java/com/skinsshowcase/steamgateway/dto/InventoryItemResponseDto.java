package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Элемент инвентаря в ответе нашего API.
 * Для скинов CS2: float, pattern (seed), wearName, коллекция, стикеры, extraAttributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Предмет в инвентаре пользователя Steam")
public class InventoryItemResponseDto {

    @Schema(description = "Уникальный идентификатор предмета (asset id)")
    private String assetId;

    @Schema(description = "Идентификатор класса предмета")
    private String classId;

    @Schema(description = "Идентификатор экземпляра")
    private String instanceId;

    @Schema(description = "Название предмета")
    private String name;

    @Schema(description = "Название для маркета")
    private String marketHashName;

    @Schema(description = "Тип предмета")
    private String type;

    @Schema(description = "Количество")
    private Integer amount;

    @Schema(description = "URL иконки")
    private String iconUrl;

    @Schema(description = "Inspect-ссылка для запроса float/pattern (CSFloat Inspect API и др.)")
    private String inspectLink;

    // --- Атрибуты скина (CS2 и др.) ---

    @Schema(description = "Float (степень износа 0–1)")
    private Double floatValue;

    @Schema(description = "Текстовое описание износа (Factory New, Field-Tested и т.д.)")
    private String wearName;

    @Schema(description = "Pattern / paint seed (индекс паттерна скина из Steam)")
    private Integer pattern;

    @Schema(description = "Название коллекции (из тегов описания Steam)")
    private String collectionName;

    @Schema(description = "Стикеры на предмете")
    private List<StickerInfoDto> stickers;

    @Schema(description = "Остальные атрибуты из описания (type → value)")
    private Map<String, String> extraAttributes;

    @Schema(description = "Минимальная цена USD из каталога items (null, если нет в каталоге или цена не заполнена)")
    private BigDecimal catalogMinPriceUsd;
}
