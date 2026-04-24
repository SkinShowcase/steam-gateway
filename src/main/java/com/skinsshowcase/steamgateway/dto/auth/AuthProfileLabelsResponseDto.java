package com.skinsshowcase.steamgateway.dto.auth;

import java.util.Map;

public record AuthProfileLabelsResponseDto(Map<String, String> labelBySteamId) {
}
