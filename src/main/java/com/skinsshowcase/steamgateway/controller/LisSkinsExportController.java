package com.skinsshowcase.steamgateway.controller;

import com.skinsshowcase.steamgateway.dto.LisSkinsExportDto;
import com.skinsshowcase.steamgateway.service.LisSkinsExportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API: прокси к lis-skins — отдаёт сырой JSON экспорта CS2.
 * Обработка и сохранение в БД — ответственность сервиса items.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market/cs2")
@Tag(name = "Lis-Skins Export", description = "Экспорт цен CS2 с lis-skins.com (сырой JSON для сервиса items)")
@RequiredArgsConstructor
public class LisSkinsExportController {

    private final LisSkinsExportService lisSkinsExportService;

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Получить экспорт CS2 с lis-skins",
            description = "Запрашивает api_csgo_full.json у lis-skins.com и возвращает ответ без изменений. " +
                    "Сервис items вызывает этот endpoint, обрабатывает данные и сохраняет в свою БД."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Экспорт получен",
                    content = @Content(schema = @Schema(implementation = LisSkinsExportDto.class))),
            @ApiResponse(responseCode = "502", description = "Ошибка при обращении к lis-skins")
    })
    public ResponseEntity<LisSkinsExportDto> getExport() {
        var dto = lisSkinsExportService.getFullExport();
        if (dto == null) {
            log.warn("Lis-skins export unavailable (null); returning 502 so client does not parse empty body");
            return ResponseEntity.status(502).build();
        }
        return ResponseEntity.ok(dto);
    }
}
