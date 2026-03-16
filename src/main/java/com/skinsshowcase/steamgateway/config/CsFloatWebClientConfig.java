package com.skinsshowcase.steamgateway.config;

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
 * WebClient для CSFloat Inspect API. Создаётся только при csfloat.inspect.enabled=true и непустом base-url.
 */
@Configuration
public class CsFloatWebClientConfig {

    @Bean("csFloatWebClient")
    public WebClient csFloatWebClient(CsFloatProperties properties) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeoutMs())
                .responseTimeout(java.time.Duration.ofMillis(properties.getReadTimeoutMs()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
