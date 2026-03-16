package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Ответ GET / CSFloat Inspect API (с параметрами s, a, d, m).
 * См. https://github.com/csfloat/inspect — Reply обёрнут в "iteminfo".
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsFloatGetResponseDto {

    @JsonProperty("iteminfo")
    private CsFloatItemInfoDto itemInfo;

    /** Ответ с ошибкой (200 + body с error/code). */
    @JsonProperty("error")
    private String error;

    @JsonProperty("code")
    private Integer code;
}
