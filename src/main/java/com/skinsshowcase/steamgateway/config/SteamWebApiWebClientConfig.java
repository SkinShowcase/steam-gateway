package com.skinsshowcase.steamgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class SteamWebApiWebClientConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Bean("steamWebApiWebClient")
    public WebClient steamWebApiWebClient(SteamWebApiProperties properties) {
        var httpClient = HttpClient.create().responseTimeout(TIMEOUT);
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
