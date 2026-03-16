package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Тело запроса POST /bulk CSFloat Inspect API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsFloatBulkRequestDto {

    @JsonProperty("bulk_key")
    private String bulkKey;

    @JsonProperty("links")
    private List<CsFloatBulkLinkDto> links;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsFloatBulkLinkDto {
        @JsonProperty("link")
        private String link;
    }
}
