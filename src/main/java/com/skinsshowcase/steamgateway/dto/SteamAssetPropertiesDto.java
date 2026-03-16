package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Блок asset_properties для одного предмета (assetid) в ответе Steam Inventory API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamAssetPropertiesDto {

    @JsonProperty("appid")
    private Integer appId;

    @JsonProperty("contextid")
    private String contextId;

    @JsonProperty("assetid")
    private String assetId;

    @JsonProperty("asset_properties")
    private List<SteamAssetPropertyDto> assetProperties;
}
