package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ API lis-skins.com: market_export_json/api_csgo_full.json.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LisSkinsExportDto {

    @JsonProperty("items")
    private List<LisSkinsItemDto> items;
}
