package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Элемент из массива descriptions ответа Steam Community Inventory API.
 * Вложенный массив descriptions содержит атрибуты предмета (float, paint seed, stickers и т.д.).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamDescriptionDto {

    @JsonProperty("classid")
    private String classId;

    @JsonProperty("instanceid")
    private String instanceId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("market_hash_name")
    private String marketHashName;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("marketable")
    private Integer marketable;

    @JsonProperty("tradable")
    private Integer tradable;

    @JsonProperty("type")
    private String type;

    /** Вложенные атрибуты: пары type/value (float, paint seed, paint index, stickers и т.д.). */
    @JsonProperty("descriptions")
    private List<SteamDescriptionItemDto> descriptionItems;

    /** Действия (например «Inspect in Game» с inspect-ссылкой steam://rungame/730/...). */
    @JsonProperty("actions")
    private List<SteamActionDto> actions;

    /** Теги (Collection, Exterior, Rarity и т.д.) — для извлечения названия коллекции. */
    @JsonProperty("tags")
    private List<SteamTagDto> tags;
}
