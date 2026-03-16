package com.skinsshowcase.steamgateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsshowcase.steamgateway.dto.StickerInfoDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсит строковое значение стикеров из атрибутов описания Steam (JSON, sticker_info HTML или текст).
 */
@Slf4j
public final class StickerParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Извлечение названий из title="Sticker: ..." в HTML sticker_info. */
    private static final Pattern TITLE_STICKER = Pattern.compile("title\\s*=\\s*[\"']Sticker:\\s*([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private StickerParser() {
    }

    /**
     * Парсит атрибут sticker_info (HTML или текст вида "Sticker: A, B, C") в список стикеров.
     * Имена извлекаются из title="Sticker: ..." или из текста после последнего &lt;/div&gt;.
     */
    public static List<StickerInfoDto> parseFromStickerInfo(String stickerInfo) {
        if (stickerInfo == null || stickerInfo.isBlank()) {
            return null;
        }
        var names = extractNamesFromTitleAttributes(stickerInfo);
        if (names.isEmpty()) {
            names = extractNamesFromTrailingText(stickerInfo);
        }
        if (names.isEmpty()) {
            return null;
        }
        var result = new ArrayList<StickerInfoDto>();
        for (var i = 0; i < names.size(); i++) {
            result.add(StickerInfoDto.builder()
                    .slot(i)
                    .stickerId(null)
                    .name(names.get(i).trim())
                    .wear(null)
                    .build());
        }
        return result;
    }

    private static List<String> extractNamesFromTitleAttributes(String html) {
        var list = new ArrayList<String>();
        Matcher m = TITLE_STICKER.matcher(html);
        while (m.find()) {
            list.add(m.group(1).trim());
        }
        return list;
    }

    /** Текст вида "Sticker: Name1, Name2, Name3" в конце блока. */
    private static List<String> extractNamesFromTrailingText(String text) {
        var list = new ArrayList<String>();
        var idx = text.lastIndexOf("Sticker:");
        if (idx < 0) {
            return list;
        }
        var tail = text.substring(idx + 8).trim();
        if (tail.isEmpty()) {
            return list;
        }
        var parts = tail.split(",\\s*");
        for (var p : parts) {
            var name = p.replaceAll("<[^>]+>", "").trim();
            if (!name.isBlank()) {
                list.add(name);
            }
        }
        return list;
    }

    /**
     * Пытается распарсить значение (например JSON-массив объектов с slot, sticker_id, wear, name).
     * Возвращает null, если формат неизвестен.
     */
    public static List<StickerInfoDto> tryParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var trimmed = value.trim();
        if (!trimmed.startsWith("[")) {
            return null;
        }
        try {
            var list = OBJECT_MAPPER.readValue(trimmed, new TypeReference<List<Map<String, Object>>>() { });
            return mapToStickerDtos(list);
        } catch (Exception e) {
            log.trace("Could not parse stickers JSON: {}", e.getMessage());
            return null;
        }
    }

    private static List<StickerInfoDto> mapToStickerDtos(List<Map<String, Object>> list) {
        var result = new ArrayList<StickerInfoDto>();
        for (var obj : list) {
            var dto = mapOneSticker(obj);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result.isEmpty() ? null : result;
    }

    private static StickerInfoDto mapOneSticker(Map<String, Object> obj) {
        var slot = getInt(obj, "slot");
        var stickerId = getLong(obj, "sticker_id");
        if (stickerId == null) {
            stickerId = getLong(obj, "id");
        }
        var name = firstNonBlank(getString(obj, "title"), getString(obj, "name"));
        var wear = getDouble(obj, "wear");
        if (slot == null && stickerId == null && name == null) {
            return null;
        }
        return StickerInfoDto.builder()
                .slot(slot)
                .stickerId(stickerId)
                .name(name)
                .wear(wear)
                .build();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b != null ? b : null;
    }

    private static Integer getInt(Map<String, Object> map, String key) {
        var v = map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long getLong(Map<String, Object> map, String key) {
        var v = map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double getDouble(Map<String, Object> map, String key) {
        var v = map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        var v = map.get(key);
        return v == null ? null : v.toString();
    }
}
