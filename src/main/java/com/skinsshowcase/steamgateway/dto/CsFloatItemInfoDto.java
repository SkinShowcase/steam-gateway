package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ CSFloat Inspect API (iteminfo).
 * См. https://github.com/csfloat/inspect — Reply.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsFloatItemInfoDto {

    @JsonProperty("itemid")
    private String itemId;

    /** Параметр "a" (asset id) в ответе API — используется как ключ при маппинге. */
    @JsonProperty("a")
    private String a;

    @JsonProperty("defindex")
    private Integer defIndex;

    @JsonProperty("paintindex")
    private Long paintIndex;

    @JsonProperty("paintseed")
    private Integer paintSeed;

    @JsonProperty("floatvalue")
    private Double floatValue;

    @JsonProperty("wear_name")
    private String wearName;

    @JsonProperty("full_item_name")
    private String fullItemName;

    @JsonProperty("weapon_type")
    private String weaponType;

    @JsonProperty("rarity_name")
    private String rarityName;

    @JsonProperty("quality_name")
    private String qualityName;

    @JsonProperty("origin_name")
    private String originName;

    @JsonProperty("imageurl")
    private String imageUrl;

    @JsonProperty("min")
    private Double minWear;

    @JsonProperty("max")
    private Double maxWear;

    @JsonProperty("stickers")
    private List<CsFloatStickerDto> stickers;

    @JsonProperty("customname")
    private String customName;

    @JsonProperty("killeatervalue")
    private Integer killeaterValue;

    /** При ошибке по одному предмету в bulk CSFloat возвращает error/code вместо iteminfo. */
    @JsonProperty("error")
    private String error;

    @JsonProperty("code")
    private Integer code;
}
