package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Фрагмент ответа GET /api/v1/items/{itemId} сервиса items (только цена и метаданные каталога).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemCatalogPriceDto {

    private String itemId;

    private String name;

    private BigDecimal minPriceUsd;

    private Instant updatedAt;
}
