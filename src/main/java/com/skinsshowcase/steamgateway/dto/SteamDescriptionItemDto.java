package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Элемент вложенного массива descriptions внутри описания предмета Steam.
 * Атрибуты предмета: name (exterior_wear, float и т.д.) + value; type обычно "html".
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamDescriptionItemDto {

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;
}
