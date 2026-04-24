package com.skinsshowcase.steamgateway.dto.steamweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamPlayerSummariesEnvelopeDto {

    @JsonProperty("response")
    private SteamPlayerSummariesResponseDto response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SteamPlayerSummariesResponseDto {

        @JsonProperty("players")
        private List<SteamPlayerSummaryPlayerDto> players;
    }
}
