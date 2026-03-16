package com.skinsshowcase.steamgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация о стикере на предмете (CS2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стикер на предмете")
public class StickerInfoDto {

    @Schema(description = "Слот (0–4)")
    private Integer slot;

    @Schema(description = "Идентификатор стикера (defindex)")
    private Long stickerId;

    @Schema(description = "Название стикера")
    private String name;

    @Schema(description = "Степень износа стикера (0–1)")
    private Double wear;
}
