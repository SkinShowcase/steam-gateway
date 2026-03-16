package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Стикер в ответе CSFloat Inspect API (формат Reply).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsFloatStickerDto {

    @JsonProperty("slot")
    private Integer slot;

    @JsonProperty("stickerId")
    @JsonAlias("sticker_id")
    private Long stickerId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("wear")
    private Double wear;
}
