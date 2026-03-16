package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Один результат из Steam Market search/render (appid=730).
 * sell_price — в центах (например 2129 = $21.29).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamMarketSearchResultDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("hash_name")
    private String hashName;

    @JsonProperty("sell_listings")
    private Integer sellListings;

    @JsonProperty("sell_price")
    private Integer sellPrice;

    @JsonProperty("sell_price_text")
    private String sellPriceText;

    @JsonProperty("asset_description")
    private SteamMarketAssetDescriptionDto assetDescription;
}
