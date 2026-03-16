package com.skinsshowcase.steamgateway;

import com.skinsshowcase.steamgateway.client.SteamClientProperties;
import com.skinsshowcase.steamgateway.config.CsFloatProperties;
import com.skinsshowcase.steamgateway.config.LisSkinsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SteamClientProperties.class, LisSkinsProperties.class, CsFloatProperties.class})
public class SteamGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamGatewayApplication.class, args);
    }
}
