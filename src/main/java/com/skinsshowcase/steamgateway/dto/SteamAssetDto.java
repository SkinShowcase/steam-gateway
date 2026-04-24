package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Элемент из массива assets ответа Steam Community Inventory API.
 * В новом API поле называется assetid (не id), pos отсутствует.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamAssetDto {

    /** В новом API приходит как "assetid". */
    @JsonProperty("assetid")
    private String id;

    @JsonProperty("classid")
    private String classId;

    @JsonProperty("instanceid")
    private String instanceId;

    @JsonProperty("amount")
    private String amount;

    /** В новом API отсутствует; оставлено для совместимости со старым форматом. */
    @JsonProperty("pos")
    private Integer pos;
}
