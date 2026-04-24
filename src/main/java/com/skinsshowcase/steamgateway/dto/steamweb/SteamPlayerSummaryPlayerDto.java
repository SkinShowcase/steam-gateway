package com.skinsshowcase.steamgateway.dto.steamweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamPlayerSummaryPlayerDto {

    @JsonProperty("steamid")
    private String steamId;

    @JsonProperty("personaname")
    private String personaName;

    @JsonProperty("avatarfull")
    private String avatarFull;

    @JsonProperty("avatarmedium")
    private String avatarMedium;
}
