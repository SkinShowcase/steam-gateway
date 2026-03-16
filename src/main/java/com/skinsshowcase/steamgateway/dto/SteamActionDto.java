package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Элемент массива actions в описании предмета Steam (например «Inspect in Game» со ссылкой).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamActionDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("link")
    private String link;
}
