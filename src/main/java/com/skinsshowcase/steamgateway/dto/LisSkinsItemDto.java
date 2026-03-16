package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Один предмет из экспорта lis-skins.com (api_csgo_full.json).
 * price — в USD.
 * item_class_id — Steam classid предмета (appid 730); используется как item_id в БД.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LisSkinsItemDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private BigDecimal price;

    /** Steam classid предмета (appid 730). В БД в item_id попадает именно он. */
    @JsonProperty("item_class_id")
    private String itemClassId;
}
