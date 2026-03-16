package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Тег из массива tags в описании предмета Steam (Collection, Exterior, Rarity и т.д.).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamTagDto {

    @JsonProperty("category")
    private String category;

    @JsonProperty("internal_name")
    private String internalName;

    @JsonProperty("localized_category_name")
    private String localizedCategoryName;

    @JsonProperty("localized_tag_name")
    private String localizedTagName;

    @JsonProperty("color")
    private String color;
}
