package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ Steam Community Inventory API: /inventory/{steamId}/{appId}/{contextId}
 * Новый формат: assets и descriptions — массивы (не rgInventory/rgDescriptions).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamInventoryResponseDto {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("assets")
    private List<SteamAssetDto> assets;

    @JsonProperty("descriptions")
    private List<SteamDescriptionDto> descriptions;

    @JsonProperty("total_inventory_count")
    private Integer totalInventoryCount;

    @JsonProperty("more_items")
    private Integer moreItems;

    @JsonProperty("last_assetid")
    private String lastAssetId;

    /** Свойства предметов: float (Wear Rating), pattern (Pattern Template) и т.д. */
    @JsonProperty("asset_properties")
    private List<SteamAssetPropertiesDto> assetProperties;
}
