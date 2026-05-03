package com.skinsshowcase.steamgateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestApi())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void invalidSteamId_returns400() throws Exception {
        mockMvc.perform(get("/__t/bad-steam"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Steam ID"));
    }

    @Test
    void inventoryItemNotFound_returns404() throws Exception {
        mockMvc.perform(get("/__t/no-item"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Inventory Item Not Found"));
    }

    @Test
    void itemsService_returns502() throws Exception {
        mockMvc.perform(get("/__t/items-down"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Items Service Error"));
    }

    @Test
    void steamApi_returns502() throws Exception {
        mockMvc.perform(get("/__t/steam-down"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Steam API Error"));
    }

    @RestController
    static class TestApi {

        @GetMapping("/__t/bad-steam")
        void badSteam() {
            throw new InvalidSteamIdException("bad");
        }

        @GetMapping("/__t/no-item")
        void noItem() {
            throw new InventoryItemNotFoundException("missing");
        }

        @GetMapping("/__t/items-down")
        void items() {
            throw new ItemsServiceException("timeout");
        }

        @GetMapping("/__t/steam-down")
        void steam() {
            throw new SteamApiException("502");
        }
    }
}
