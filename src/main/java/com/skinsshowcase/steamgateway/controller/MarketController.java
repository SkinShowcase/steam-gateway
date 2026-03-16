package com.skinsshowcase.steamgateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API: маркет Steam (экспорт CS2, синк и т.д.).
 * Ссылка на изображение предмета строится по шаблону в сервисе items (item_id + size).
 */
@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {
}
