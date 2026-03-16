package com.skinsshowcase.steamgateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * WebClient для вызовов lis-skins.com (экспорт цен CS2).
 */
@Configuration
public class LisSkinsWebClientConfig {

    @Bean("lisSkinsWebClient")
    public WebClient lisSkinsWebClient(LisSkinsProperties properties) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeoutMs())
                .responseTimeout(java.time.Duration.ofMillis(properties.getReadTimeoutMs()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        var strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(properties.getMaxInMemorySizeBytes()))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
