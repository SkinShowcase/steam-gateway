package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Элемент инвентаря в ответе нашего API.
 * Для скинов CS2 включены float, pattern, степень износа, коллекция и прочие атрибуты.
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

    @Schema(description = "Paint seed (паттерн/рисунок скина)")
    private Integer paintSeed;

    @Schema(description = "Paint index (ID скина/краски, привязка к коллекции)")
    private Long paintIndex;

    @Schema(description = "Текстовое описание износа (Factory New, Field-Tested и т.д.)")
    private String wearName;

    @Schema(description = "Pattern (индекс паттерна/рисунка скина из Steam asset_properties)")
    private Integer pattern;

    @Schema(description = "Название коллекции (из тегов описания Steam)")
    private String collectionName;

    @Schema(description = "Стикеры на предмете")
    private List<StickerInfoDto> stickers;

    // --- Доп. поля в формате CSFloat Inspect API Reply ---

    @Schema(description = "Полное имя предмета (напр. AK-47 | Redline (Field-Tested))")
    private String fullItemName;

    @Schema(description = "Тип оружия (напр. AK-47)")
    private String weaponType;

    @Schema(description = "Редкость (напр. Classified)")
    private String rarityName;

    @Schema(description = "Качество (напр. Unique, StatTrak™)")
    private String qualityName;

    @Schema(description = "Происхождение (напр. Found in Crate)")
    private String originName;

    @Schema(description = "Минимальный износ скина (float)")
    private Double minWear;

    @Schema(description = "Максимальный износ скина (float)")
    private Double maxWear;

    @Schema(description = "Кастомное имя (nametag)")
    private String customName;

    @Schema(description = "StatTrak: счётчик убийств")
    private Integer killeaterValue;

    @Schema(description = "Defindex оружия (weapon ID)")
    private Integer defIndex;

    @Schema(description = "Остальные атрибуты из описания (type → value)")
    private Map<String, String> extraAttributes;
}
