# steam-gateway (Skins Showcase)

Интеграции со Steam и внешними источниками данных для скинов:

- **Инвентарь** Steam Community (без Steam Web API ключа; инвентарь должен быть публичным).
- **Прокси экспорта цен CS2** с lis-skins (`api_csgo_full.json`) — «как есть», дальше разбор делает `items`.
- **Каталог цен/обогащение**: запросы к `items` (`items-catalog.base-url`).
- **Публичные подписи в инвентаре**: запросы к `auth` для display name (`auth-service.*` + `AUTH_INTERNAL_SERVICE_KEY`).
- **Кеширование**: Redis (см. `steam-gateway.cache.redis-enabled`, Spring Data Redis).

Репозиторий: https://github.com/SkinShowcase/steam-gateway  
Инфраструктура (compose): https://github.com/SkinShowcase/infrastructure

## Порт

- По умолчанию **8080** (`server.port`)

## Публичные REST эндпоинты

### Инвентарь

Источник: `InventoryController` (`/api/v1/...`)

- `GET /api/v1/inventory/{steamId}?appId=730&contextId=2`
  - tradable-only, фильтрация «медалей», кеш списка (см. описание в OpenAPI аннотациях)
  - `personaName` собирается из `auth`, иначе Steam Web API summary
- `GET /api/v1/inventory/{steamId}/item?assetId=...&classId=...&appId=730&contextId=2`
  - один предмет + `catalogMinPriceUsd` из `items`

### Экспорт lis-skins (сырой JSON)

Источник: `LisSkinsExportController` (`/api/v1/market/cs2/...`)

- `GET /api/v1/market/cs2/export` — проксирует `lis-skins.export-url`, возвращает DTO/JSON; при ошибке может ответить `502` без тела (см. код)

`MarketController` сейчас **пустой** (зарезервирован под `/api/v1/market`), не ожидайте дополнительных публичных методов там.

## Внутренние эндпоинты (не через API Gateway)

### Профиль Steam для auth

`InternalSteamProfileController`:

- `GET /internal/v1/steam/profile/{steamId}` (`@Hidden` в OpenAPI)

### Инвалидация кеша после синка каталога

`InternalCacheEvictController`:

- `POST /internal/v1/cache/evict` + заголовок `X-Internal-Token: <STEAM_GATEWAY_CACHE_EVICT_TOKEN>`

## Конфигурация (минимум)

| Переменная | Зачем |
|------------|------|
| `STEAM_API_KEY` | Для Steam Web API вызовов (часть обогащения/профиля; в compose обычно обязателен) |
| `SPRING_DATA_REDIS_HOST/PORT` | Redis для кеша |
| `STEAM_GATEWAY_CACHE_EVICT_TOKEN` | Токен для `POST /internal/v1/cache/evict` |
| `ITEMS_CATALOG_BASE_URL` | URL сервиса `items` |
| `AUTH_SERVICE_BASE_URL` + `AUTH_INTERNAL_SERVICE_KEY` | Доступ к internal API `auth` |

## Наблюдаемость

- `/actuator/health`, `/actuator/prometheus`
- `/swagger-ui.html`, `/api-docs`

## Запуск локально

```bash
./gradlew bootRun
```

## Docker

```bash
docker build -t skins-showcase/steam-gateway .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker skins-showcase/steam-gateway
```
