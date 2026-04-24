package com.skinsshowcase.steamgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsPriceBatchRequestDto {

    @JsonProperty("itemIds")
    private List<String> itemIds;
}
