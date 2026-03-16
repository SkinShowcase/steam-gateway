package com.skinsshowcase.steamgateway.service;

import com.skinsshowcase.steamgateway.client.LisSkinsClient;
import com.skinsshowcase.steamgateway.dto.LisSkinsExportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис экспорта lis-skins: единственная ответственность — получить JSON из lis-skins и отдать наружу.
 * Обработка и сохранение данных — зона ответственности сервиса items.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LisSkinsExportService {

    private final LisSkinsClient lisSkinsClient;

    /**
     * Загружает полный экспорт CS2 с lis-skins.com (api_csgo_full.json) и возвращает как есть.
     */
    public LisSkinsExportDto getFullExport() {
        var dto = lisSkinsClient.getFullExport().blockOptional().orElse(null);
        if (dto != null) {
            log.debug("Lis-skins export ready for consumer");
        }
        return dto;
    }
}
