# steam-gateway

Микросервис **Skins Showcase**: интеграция с Steam (Steam Community API) и **lis-skins.com**. Отдаёт профильные данные: инвентарь пользователя по Steam ID и предметы CS2 с ценами (синхронизация с lis-skins, хранение в БД для валидации и отображения цен).

## Стек

- **Java 21**, Spring Boot 3.2.x
- **REST** (JSON), WebClient для вызовов Steam и lis-skins
- **JPA + PostgreSQL** (или H2 для разработки) — хранение предметов CS2 (item_id, название, min price USD)
- **OpenAPI/Swagger**, **Prometheus** metrics, структурированные логи
- **Resilience4j** retry с backoff, таймауты, валидация SteamID64; retry для lis-skins при 5xx/таймаутах

## Запуск

**По умолчанию (H2, без установки БД):**

```bash
./gradlew bootRun
```

**С локальной PostgreSQL 16 (localhost:5432, пользователь postgres):**

```bash
# Windows (PowerShell): задать пароль и запустить с профилем local
$env:SPRING_DATASOURCE_PASSWORD = "ваш_пароль"
./gradlew bootRun --args="--spring.profiles.active=local"

# Или указать свою БД (по умолчанию — postgres):
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/steam_gateway"
$env:SPRING_DATASOURCE_PASSWORD = "ваш_пароль"
./gradlew bootRun --args="--spring.profiles.active=local"
```

**Если запускаете из IntelliJ IDEA:** в Run Configuration в поле **Active profiles** укажите только `local` (без `--spring.profiles.active=`). В **Environment variables** добавьте `SPRING_DATASOURCE_PASSWORD=ваш_пароль`. Иначе приложение подключится к H2, а не к PostgreSQL.

Порт по умолчанию: **8080**. Профиль `local` подключается к PostgreSQL на localhost:5432, username: postgres; пароль только из переменной окружения (не хардкодится).

## API

### 1. Инвентарь по Steam ID

```http
GET /api/v1/inventory/{steamId}?appId=730&contextId=2
```

| Параметр    | Тип   | По умолчанию | Описание |
|-------------|--------|--------------|----------|
| `steamId`   | path   | —            | SteamID64 (17 цифр, например `76561198000000000`) |
| `appId`     | query  | 730          | App ID (730 — CS2/CS:GO, 753 — Steam) |
| `contextId` | query  | 2            | Context ID (2 — инвентарь CS2) |

**Пример запроса:**

```bash
curl -s "http://localhost:8080/api/v1/inventory/76561198000000000"
```

**Пример ответа (200):**

```json
{
  "steamId": "76561198000000000",
  "appId": 730,
  "contextId": 2,
  "items": [
    {
      "assetId": "31234567890",
      "classId": "469437901",
      "instanceId": "302028390",
      "name": "AK-47 | Redline (Field-Tested)",
      "marketHashName": "AK-47 | Redline (Field-Tested)",
      "type": "Rifle",
      "amount": 1,
      "iconUrl": "https://..."
    }
  ]
}
```

**Edge-cases (инвентарь):**

- **Приватный инвентарь** — Steam вернёт пустой или ошибку → 502, тело с сообщением об ошибке Steam API.
- **Неверный Steam ID** (не 17 цифр / не начинается с 765) → 400, Problem Detail с сообщением валидации.
- **Несуществующий Steam ID** — Steam может вернуть пустой список или success=false → 200 с пустым `items`.
- **Таймаут / 5xx Steam** — retry до 3 попыток с backoff, затем 502.
- **floatValue, paintSeed и т.д. = null** — обогащение через CSFloat Inspect включается при `csfloat.inspect.enabled=true`. Бот понимает только **legacy**-формат inspect-ссылки (`steam://rungame/730/.../S...A...D...`), см. [csfloat/inspect API](https://github.com/csfloat/inspect#api). Ссылки **CS2** (`steam://run/730/...`, сериализованный hex или заглушка `%propid:6%`) не отправляются в бот; для таких предметов float остаётся null до поддержки нового формата в боте.
- **Формат полей скина** — при успешном ответе бота ответ API по каждому предмету приведён к [формату Reply csfloat/inspect](https://github.com/csfloat/inspect#reply): `floatValue`, `paintSeed`, `paintIndex`, `wearName`, `fullItemName`, `weaponType`, `rarityName`, `qualityName`, `originName`, `minWear`, `maxWear`, `customName`, `killeaterValue`, `defIndex`, `stickers` (slot, stickerId, name, wear).

---

### 2. CS2 Market — синхронизация и список предметов

Предметы CS2 (название, минимальная цена в USD) загружаются с **lis-skins.com** (экспорт `api_csgo_full.json`) и сохраняются в БД для валидации и отображения цен. В **item_id** попадает именно **Steam classid** предмета, если Lis Skins его отдаёт в экспорте; иначе используется fallback — стабильный хэш по имени (SHA-256). Идентификатор Lis Skins в item_id не используется.

**Запустить синхронизацию CS2 с lis-skins:**

```http
POST /api/v1/market/cs2/sync
```

Запускает **фоновую джобу**: один запрос к lis-skins, группировка по имени (min цена), батч-запись в БД. Ответ возвращается сразу; прогресс и результат — в логах приложения. Повторный вызов во время выполнения джобы игнорируется.

**Пример запроса:**

```bash
curl -X POST "http://localhost:8080/api/v1/market/cs2/sync"
```

**Пример ответа (200):**

```json
{
  "message": "Market sync job started. Progress in console logs."
}
```

**Список предметов CS2 из БД:**

```http
GET /api/v1/market/cs2/items
```

**Пример ответа (200):**

```json
[
  {
    "itemId": "a1b2c3d4e5f6789012345678abcdef01",
    "name": "AK-47 | Redline (Field-Tested)",
    "minPriceUsd": 12.50,
    "updatedAt": "2025-02-02T12:00:00Z"
  }
]
```

**Edge-cases (Market):**

- **Ошибка lis-skins** (таймаут, 5xx, недоступность) → джоба логирует ошибку, в БД остаются старые данные; retry с backoff до 3 попыток.
- **Пустая БД до первого sync** → GET `/items` возвращает пустой массив.
- **Повторный POST /sync во время джобы** → 200, сообщение о том, что джоба уже запущена (новая не стартует).

## Документация и метрики

| Ресурс | URL |
|--------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |
| **Health** | http://localhost:8080/actuator/health |
| **Prometheus** | http://localhost:8080/actuator/prometheus |

## Конфигурация

Ключи в `application.yml` (или переменные окружения):

**lis-skins (источник цен CS2 для sync)**

| Свойство | По умолчанию | Описание |
|----------|--------------|----------|
| `lis-skins.export-url` | https://lis-skins.com/market_export_json/api_csgo_full.json | URL экспорта CS2 |
| `lis-skins.connect-timeout-ms` | 5000 | Таймаут подключения (мс) |
| `lis-skins.read-timeout-ms` | 30000 | Таймаут чтения (мс) |
| `lis-skins.max-retries` | 3 | Повторы при 5xx/таймауте (backoff 2s) |

**Steam Client**

| Свойство | По умолчанию | Описание |
|----------|--------------|----------|
| `steam.client.base-url` | https://steamcommunity.com | Базовый URL Steam Community |
| `steam.client.inventory-path-template` | /inventory/{steamId}/{appId}/{contextId} | Шаблон пути инвентаря |
| `steam.client.market-search-path-template` | /market/search/render/?norender=1&appid=730&... | Шаблон поиска маркета (если понадобится) |
| `steam.client.connect-timeout-ms` | 5000 | Таймаут подключения (мс) |
| `steam.client.read-timeout-ms` | 15000 | Таймаут чтения (мс) |

**Steam Market (настройки батчей и пауз, для возможного будущего использования Steam Market)**

| Свойство | По умолчанию | Описание |
|----------|--------------|----------|
| `steam.market.page-size` | 100 | Размер страницы при обходе маркета |
| `steam.market.max-pages` | 0 | Максимум страниц за один sync; 0 = все страницы (лимит 5000) |
| `steam.market.delay-between-pages-ms` | 1500 | Задержка между запросами (мс) |
| `steam.market.save-batch-size` | 1000 | Размер батча сохранения в БД при sync (0 = один раз в конце) |
| `steam.market.empty-response-retries` | 3 | Повторы при пустом ответе (backoff 2s/5s/10s) |
| `steam.market.long-pause-every-pages` | 20 | Каждые N страниц — длинная пауза (мс); 0 = отключено |
| `steam.market.long-pause-ms` | 5000 | Длительность длинной паузы (мс) |
| `steam.market.rate-limit-retry-backoff-ms` | 20000 | При 429: пауза перед повтором (мс) |
| `steam.market.rate-limit-max-retries` | 3 | Макс. повторов при 429 |

**БД и миграции**

- По умолчанию (без профиля): **H2** (файловая БД `./data/steamgateway`), для разработки без установки PostgreSQL.
- Профиль **local**: PostgreSQL на localhost:5432, пользователь postgres. Пароль **обязательно** задаётся через `SPRING_DATASOURCE_PASSWORD` (не хардкодится).
- Профиль **prod**: все параметры БД из env (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

**Миграции** выполняются автоматически при старте приложения (Flyway). Скрипты лежат в `src/main/resources/db/migration/`. Чтобы только применить миграции к своей БД без запуска сервера, можно один раз запустить приложение с нужным профилем — Flyway создаст таблицы при первом старте.

**Как подключиться к своей БД и наполнить её данными (lis-skins):**

1. Задайте переменные окружения (подставьте свои URL, пользователь и пароль):
   ```bash
   # Windows PowerShell
   $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/ваша_бд"
   $env:SPRING_DATASOURCE_USERNAME = "ваш_пользователь"
   $env:SPRING_DATASOURCE_PASSWORD = "ваш_пароль"
   ```
2. Запустите приложение с профилем `local` (или `prod`):
   ```bash
   ./gradlew bootRun --args="--spring.profiles.active=local"
   ```
   При старте Flyway применит миграции и создаст таблицу `market_item`, если её ещё нет.
3. Вызовите синхронизацию с lis-skins:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/market/cs2/sync"
   ```
   Джоба в фоне загрузит экспорт CS2 с lis-skins.com и сохранит данные в таблицу `market_item`. Прогресс — в логах приложения.

| Свойство | Профиль local | Описание |
|----------|----------------|----------|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/postgres | URL PostgreSQL (можно переопределить) |
| `SPRING_DATASOURCE_USERNAME` | postgres | Пользователь |
| `SPRING_DATASOURCE_PASSWORD` | — | **Обязательно** из env |

### Почему не вижу БД и таблицу? Как посмотреть содержимое локально

Зависит от того, как вы запускаете приложение.

**Вариант 1: Запуск без профиля (H2, по умолчанию)**

- БД — это **файл на диске**, а не отдельный сервер. Он создаётся при **первом** запуске приложения.
- **Где искать:** папка `data/` в корне проекта, файл `steamgateway.mv.db`:
  ```
  C:\Users\Sasha\IdeaProjects\steam-gateway\data\steamgateway.mv.db
  ```
  Папка `data/` в `.gitignore`, поэтому в проводнике она может быть скрыта или не отображаться в дереве проекта IDE — откройте папку `IdeaProjects\steam-gateway` в проводнике Windows и зайдите в `data`.
- **Как посмотреть таблицу:**
  1. Запустите приложение: `.\gradlew.bat bootRun`
  2. Откройте в браузере: **http://localhost:8080/h2-console**
  3. В форме входа укажите:
     - **JDBC URL:** `jdbc:h2:file:./data/steamgateway` (если консоль не находит файл — укажите полный путь: `jdbc:h2:file:C:/Users/Sasha/IdeaProjects/steam-gateway/data/steamgateway`)
     - **User Name:** `sa`
     - **Password:** пусто
  4. Нажмите Connect, затем выполните SQL: `SELECT * FROM market_item;`
- **Важно:** таблица `market_item` будет **пустой**, пока вы не вызвали синхронизацию с lis-skins: `POST http://localhost:8080/api/v1/market/cs2/sync`.

**Вариант 2: Запуск с профилем `local` (PostgreSQL)**

- БД здесь **не файл в проекте**, а **сервер PostgreSQL** на вашем компьютере (localhost:5432).
- **Что проверить:**
  1. Установлен ли PostgreSQL и запущен ли сервис (порт 5432).
  2. Пароль задан в переменной: `$env:SPRING_DATASOURCE_PASSWORD = "ваш_пароль"`.
- **Как посмотреть таблицу:** подключитесь к PostgreSQL любым клиентом:
  - **IntelliJ IDEA:** окно Database → ваш источник `postgres@localhost` (или новый Data Source: PostgreSQL, host localhost, port 5432, database `postgres`, user `postgres`, пароль из env). После подключения откройте схему `public` → таблица `market_item` → правый клик → View → Data.
  - **DBeaver / pgAdmin:** новое подключение PostgreSQL (localhost:5432, база `postgres`, пользователь `postgres`, ваш пароль), затем схема `public`, таблица `market_item`.
- Таблица снова будет пустой, пока не выполнен `POST /api/v1/market/cs2/sync`.

**Кратко:** H2 — смотрите файл в `data/steamgateway.mv.db` и веб-консоль `/h2-console`; PostgreSQL — подключайтесь к localhost:5432 клиентом (IntelliJ, DBeaver, pgAdmin). В обоих случаях данные появятся только после вызова синхронизации с lis-skins.

Секреты и API-ключи для инвентаря (Steam) и цен (lis-skins) не используются — публичные эндпоинты.

## Структура проекта

```
com.skinsshowcase.steamgateway
├── SteamGatewayApplication.java
├── client/           # SteamClient, SteamClientProperties; LisSkinsClient — изоляция вызовов Steam и lis-skins
├── config/           # SteamWebClientConfig, LisSkinsWebClientConfig, MarketProperties, LisSkinsProperties, AsyncConfig
├── controller/       # InventoryController, MarketController
├── dto/              # Steam* DTO, Inventory* DTO, LisSkinsExportDto, LisSkinsItemDto, MarketItemResponseDto
├── entity/           # MarketItem (JPA)
├── exception/        # SteamApiException, InvalidSteamIdException, GlobalExceptionHandler
├── repository/       # MarketItemRepository
└── service/          # InventoryService, MarketService (sync CS2 с lis-skins, асинхронная джоба)
```

## Надёжность и безопасность

- **Retry:** Resilience4j retry для Steam WebClient; для lis-skins — retry с backoff в `LisSkinsClient` (`lis-skins.max-retries`).
- **Таймауты:** connect/read для Steam в `steam.client.*`, для lis-skins в `lis-skins.*`.
- **Асинхронный sync:** джоба синхронизации CS2 выполняется в отдельном executor (`AsyncConfig`); повторный вызов во время выполнения не стартует новую джобу.
- **Валидация:** SteamID64 по паттерну; некорректные данные → 400 и Problem Detail.
- **Секреты:** только через env/config, не логируются.
