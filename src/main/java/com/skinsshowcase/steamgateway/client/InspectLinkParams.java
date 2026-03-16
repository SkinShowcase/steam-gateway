package com.skinsshowcase.steamgateway.client;

import lombok.Builder;
import lombok.Value;

import java.util.regex.Pattern;

/**
 * Параметры из inspect-ссылки для запроса GET / CSFloat API (s, a, d, m).
 * См. https://github.com/csfloat/inspect — GET / с параметрами s, a, d, m.
 */
@Value
@Builder
public class InspectLinkParams {

    /** Steam ID владельца (для инвентаря, параметр S в ссылке). */
    String s;
    /** Asset ID (параметр A). */
    String a;
    /** ID для запроса (параметр D). */
    String d;
    /** Market listing ID (для маркета, параметр M в ссылке). */
    String m;

    /** Допускаем пробел/плюс перед S (inventory). */
    private static final Pattern INVENTORY_PARAMS = Pattern.compile("[\\s+]?S(\\d+)A(\\d+)D(\\d+)");
    private static final Pattern MARKET_PARAMS = Pattern.compile("[\\s+]?M(\\d+)A(\\d+)D(\\d+)");

    /**
     * Парсит legacy inspect-ссылку и извлекает s, a, d, m.
     * Формат: ...csgo_econ_action_preview S76561198084749846A698323590D7935523998312483177
     * или M625254122282020305A6760346663D30614827701953021 для маркета.
     *
     * @param inspectLink полная ссылка (можно URL-encoded)
     * @return параметры или null, если ссылка не распознана
     */
    public static InspectLinkParams fromInspectLink(String inspectLink) {
        if (inspectLink == null || inspectLink.isBlank()) {
            return null;
        }
        var decoded = decodeLink(inspectLink);
        var invMatcher = INVENTORY_PARAMS.matcher(decoded);
        if (invMatcher.find()) {
            return InspectLinkParams.builder()
                    .s(invMatcher.group(1))
                    .a(invMatcher.group(2))
                    .d(invMatcher.group(3))
                    .m(null)
                    .build();
        }
        var marketMatcher = MARKET_PARAMS.matcher(decoded);
        if (marketMatcher.find()) {
            return InspectLinkParams.builder()
                    .s(null)
                    .a(marketMatcher.group(2))
                    .d(marketMatcher.group(3))
                    .m(marketMatcher.group(1))
                    .build();
        }
        return null;
    }

    private static String decodeLink(String link) {
        try {
            return java.net.URLDecoder.decode(link, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return link;
        }
    }
}
