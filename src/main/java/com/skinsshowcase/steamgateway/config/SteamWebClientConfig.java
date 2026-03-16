package com.skinsshowcase.steamgateway.config;

import com.skinsshowcase.steamgateway.client.SteamClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * WebClient для вызовов Steam Community API с таймаутами и (опционально) retry через Resilience4j.
 */
@Configuration
public class SteamWebClientConfig {

    @Bean("steamWebClient")
    public WebClient steamWebClient(SteamClientProperties properties) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeoutMs())
                .responseTimeout(properties.getReadTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
