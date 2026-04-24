package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Публичный профиль Steam (GetPlayerSummaries)")
public class SteamProfileResponseDto {

    @Schema(description = "SteamID64")
    String steamId;

    @Schema(description = "Отображаемый ник в Steam")
    String personaName;

    @Schema(description = "URL аватара (полный)")
    String avatarUrl;

    @Schema(description = "URL аватара (средний)")
    String avatarMediumUrl;
}
