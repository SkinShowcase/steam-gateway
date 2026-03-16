package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Одно свойство из asset_properties ответа Steam (Wear Rating, Pattern Template и т.д.).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamAssetPropertyDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("propertyid")
    private Integer propertyId;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("int_value")
    private String intValue;

    @JsonProperty("float_value")
    private Double floatValue;

    @JsonProperty("string_value")
    private String stringValue;
}
