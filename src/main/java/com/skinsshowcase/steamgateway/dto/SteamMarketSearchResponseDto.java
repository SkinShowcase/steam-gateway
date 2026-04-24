package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ Steam Market search/render (norender=1, appid=730).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamMarketSearchResponseDto {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("total_count")
    private Integer totalCount;

    /** Фактический размер страницы в ответе Steam (API часто возвращает не более 10, игнорируя count). */
    @JsonProperty("pagesize")
    private Integer pagesize;

    @JsonProperty("results")
    private List<SteamMarketSearchResultDto> results;
}
