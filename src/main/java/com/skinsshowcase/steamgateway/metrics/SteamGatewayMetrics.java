package com.skinsshowcase.steamgateway.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/**
 * Метрики для Grafana: инвентарь, каталог цен, исходящие HTTP к Steam / items / lis-skins / CSFloat.
 */
@Component
public class SteamGatewayMetrics {

    private final MeterRegistry registry;

    public SteamGatewayMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordInventoryRequest(String source, String outcome) {
        registry.counter("steam.gateway.inventory.requests",
                "source", source,
                "outcome", outcome).increment();
    }

    public void recordCatalogPriceStrategy(String strategy) {
        registry.counter("steam.gateway.catalog.prices.requests",
                "strategy", strategy).increment();
    }

    public void recordCatalogBatchClassIds(int missingCount) {
        if (missingCount <= 0) {
            return;
        }
        registry.summary("steam.gateway.catalog.prices.batch.classids")
                .record(missingCount);
    }

    public <T> Mono<T> traceOutbound(Mono<T> mono, String client, String operation) {
        var sample = Timer.start(registry);
        return mono.doFinally(st -> completeOutbound(sample, client, operation, st));
    }

    private void completeOutbound(Timer.Sample sample, String client, String operation, SignalType st) {
        var timer = registry.timer("steam.gateway.outbound.duration",
                "client", client,
                "operation", operation);
        sample.stop(timer);
        registry.counter("steam.gateway.outbound.requests",
                "client", client,
                "operation", operation,
                "outcome", outcomeForSignal(st)).increment();
    }

    private static String outcomeForSignal(SignalType st) {
        if (st == SignalType.ON_COMPLETE) {
            return "success";
        }
        if (st == SignalType.ON_ERROR) {
            return "error";
        }
        if (st == SignalType.CANCEL) {
            return "cancel";
        }
        return "unknown";
    }
}
