package com.skinsshowcase.steamgateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SteamWebApiProperties {

    private final String baseUrl;
    private final String key;

    public SteamWebApiProperties(
            @Value("${steam.web-api.base-url}") String baseUrl,
            @Value("${steam.web-api.key}") String key) {
        this.baseUrl = baseUrl;
        this.key = key;
    }
}
