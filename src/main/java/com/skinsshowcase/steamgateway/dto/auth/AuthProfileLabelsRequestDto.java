package com.skinsshowcase.steamgateway.dto.auth;

import java.util.List;

public record AuthProfileLabelsRequestDto(List<String> steamIds) {
}
