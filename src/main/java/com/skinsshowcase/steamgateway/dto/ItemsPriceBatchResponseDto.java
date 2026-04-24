package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ItemsPriceBatchResponseDto {

    @JsonProperty("prices")
    private Map<String, ItemCatalogPriceDto> prices;
}
