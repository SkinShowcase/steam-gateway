package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Описание предмета в ответе Steam Market search/render.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamMarketAssetDescriptionDto {

    @JsonProperty("classid")
    private String classId;

    @JsonProperty("instanceid")
    private String instanceId;

    @JsonProperty("market_hash_name")
    private String marketHashName;

    @JsonProperty("market_name")
    private String marketName;

    @JsonProperty("name")
    private String name;
}
