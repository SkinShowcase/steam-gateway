package com.skinsshowcase.steamgateway.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Component
@Validated
@Getter
public class AuthServiceProperties {

    private final String baseUrl;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final String internalServiceKey;

    public AuthServiceProperties(
            @NotBlank @Value("${auth-service.base-url}") String baseUrl,
            @Positive @Value("${auth-service.connect-timeout-ms}") long connectTimeoutMs,
            @Positive @Value("${auth-service.read-timeout-ms}") long readTimeoutMs,
            @Value("${auth-service.internal-service-key:}") String internalServiceKey) {
        this.baseUrl = baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.internalServiceKey = internalServiceKey;
    }

    public Duration getConnectTimeout() {
        return Duration.ofMillis(connectTimeoutMs);
    }

    public Duration getReadTimeout() {
        return Duration.ofMillis(readTimeoutMs);
    }
}
