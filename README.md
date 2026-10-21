# Binance Price Service

REST API-сервис на **Java 21 и Spring Boot**, который периодически получает среднюю цену торговой пары с Binance, сохраняет историю в PostgreSQL и предоставляет эндпоинты для получения последней цены, средней цены за период, истории цен и направления изменения цены.

По умолчанию сервис отслеживает торговую пару `BNBUSDT` с интервалом опроса 30 секунд.

## Содержание

- [Возможности](#возможности)
- [Технологии](#технологии)
- [Требования](#требования)
- [Запуск локально](#запуск-локально)
- [Запуск через Docker](#запуск-через-docker)
- [Переменные окружения](#переменные-окружения)
- [REST API](#rest-api)
- [Обработка ошибок](#обработка-ошибок)
- [Тестирование](#тестирование)
- [Структура проекта](#структура-проекта)

## Возможности

- Автоматический сбор цен с Binance по расписанию.
- Сохранение истории цен в PostgreSQL.
- Получение последней сохранённой цены.
- Расчёт средней цены за указанный период.
- Получение истории цен с пагинацией.
- Определение тренда цены: `UPWARD`, `DOWNWARD` или `FLAT`.
- Управление схемой базы данных с помощью Flyway.

## Технологии

- Java 21
- Spring Boot
- Spring WebMVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Gradle
- Docker и Docker Compose

## Требования

Для локального запуска необходимы:

- JDK 21;
- PostgreSQL;
- доступ к API Binance: `https://api.binance.com`.

Для запуска в контейнерах необходимы Docker Engine и Docker Compose.

## Запуск локально

### 1. Создание базы данных

Создайте пользователя и базу данных PostgreSQL:

```sql
CREATE USER binance_user WITH PASSWORD 'change_me';
CREATE DATABASE "binance-price-service" OWNER binance_user;
```

### 2. Настройка переменных окружения

**Linux / macOS:**

```bash
export DB_USERNAME=binance_user
export DB_PASSWORD=change_me
export BINANCE_SYMBOL=BNBUSDT
export BINANCE_POLL_INTERVAL=30000
```

**Windows PowerShell:**

```powershell
$env:DB_USERNAME = "binance_user"
$env:DB_PASSWORD = "change_me"
$env:BINANCE_SYMBOL = "BNBUSDT"
$env:BINANCE_POLL_INTERVAL = "30000"
```

### 3. Запуск приложения

**Linux / macOS:**

```bash
./gradlew bootRun
```

**Windows:**

```powershell
.\gradlew.bat bootRun
```

После запуска приложение будет доступно по адресу:

`http://localhost:8080`

При старте Flyway автоматически применяет миграции базы данных. PostgreSQL должен быть запущен, а учётные данные — корректны.

По умолчанию используется JDBC URL:

```text
jdbc:postgresql://localhost:5432/binance-price-service
```

При необходимости URL можно переопределить через `SPRING_DATASOURCE_URL`.

## Запуск через Docker

Если в корне проекта находятся `Dockerfile` и `compose.yaml` с описанием приложения и PostgreSQL, запустите:

```bash
docker compose up --build
```

Для запуска в фоновом режиме:

```bash
docker compose up --build -d
```

Просмотр логов приложения:

```bash
docker compose logs -f app
```

Остановка контейнеров:

```bash
docker compose down
```

Для остановки контейнеров с удалением томов и данных PostgreSQL:

```bash
docker compose down -v
```

> Внимание: команда `docker compose down -v` удаляет данные в томах Compose. Используйте её только в том случае, если хотите удалить сохранённые данные базы.

При запуске в Docker URL базы данных должен использовать имя сервиса PostgreSQL внутри Docker-сети, например:

```text
jdbc:postgresql://db:5432/binance-price-service
```

Использовать `localhost` для подключения приложения к отдельному контейнеру PostgreSQL нельзя.

## Переменные окружения

| Переменная | Описание | Значение по умолчанию |
|---|---|---|
| `DB_USERNAME` | Имя пользователя PostgreSQL | Обязательная |
| `DB_PASSWORD` | Пароль пользователя PostgreSQL | Обязательная |
| `BINANCE_SYMBOL` | Торговая пара для сбора цены | `BNBUSDT` |
| `BINANCE_POLL_INTERVAL` | Интервал сбора цены в миллисекундах | `30000` |
| `SPRING_DATASOURCE_URL` | JDBC URL базы данных | `jdbc:postgresql://localhost:5432/binance-price-service` |

### Примечание о конфигурации сборщика

Чтобы переменные `BINANCE_SYMBOL` и `BINANCE_POLL_INTERVAL` корректно применялись к текущему классу `CollectorProperties` и планировщику, в `src/main/resources/application.yaml` рекомендуется использовать следующие ключи:

```yaml
binance:
  symbol: ${BINANCE_SYMBOL:BNBUSDT}
  poll-interval-ms: ${BINANCE_POLL_INTERVAL:30000}
```

Имена свойств должны соответствовать полям класса `CollectorProperties`. Если в конфигурации оставить `symbols` и `fixed-delay-ms`, как в исходном варианте файла, настройки могут не примениться к сборщику.

## REST API

Базовый URL всех эндпоинтов:

```text
http://localhost:8080/api/v1/prices
```

Все эндпоинты используют HTTP-метод `GET` и возвращают данные в формате JSON.

### 1. Получение последней цены

**Эндпоинт:**

```http
GET /api/v1/prices/last
```

**Пример запроса:**

```bash
curl http://localhost:8080/api/v1/prices/last
```

**Пример ответа:**

```json
{
  "symbol": "BNBUSDT",
  "price": 620.50,
  "fetchedAt": "2025-01-02T12:30:00Z"
}
```

Возвращает последнюю сохранённую запись с ценой и временем получения.

### 2. Получение средней цены за период

**Эндпоинт:**

```http
GET /api/v1/prices/average?from={from}&to={to}
```

**Параметры:**

| Параметр | Тип | Описание |
|---|---|---|
| `from` | ISO-8601 | Начало периода |
| `to` | ISO-8601 | Конец периода |

**Пример запроса:**

```bash
curl "http://localhost:8080/api/v1/prices/average?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z"
```

**Пример ответа:**

```json
{
  "averagePrice": 615.25
}
```

Возвращает среднюю цену записей за указанный интервал. Если данных за период нет, сервис сообщает об отсутствии записей.

### 3. Получение истории цен с пагинацией

**Эндпоинт:**

```http
GET /api/v1/prices?from={from}&to={to}&page={page}&size={size}
```

**Параметры:**

| Параметр | Тип | Описание |
|---|---|---|
| `from` | ISO-8601 | Начало периода |
| `to` | ISO-8601 | Конец периода |
| `page` | `int` | Номер страницы, начиная с `0` |
| `size` | `int` | Количество записей на странице, от `1` до `200` |

**Пример запроса:**

```bash
curl "http://localhost:8080/api/v1/prices?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z&page=0&size=20"
```

**Пример ответа:**

```json
{
  "recordsList": [
    {
      "symbol": "BNBUSDT",
      "price": 620.50,
      "fetchedAt": "2025-01-02T12:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "hasNext": false
}
```

Записи сортируются по времени получения `fetchedAt` по возрастанию.

### 4. Определение тренда цены

**Эндпоинт:**

```http
GET /api/v1/prices/trend?from={from}&to={to}
```

**Пример запроса:**

```bash
curl "http://localhost:8080/api/v1/prices/trend?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z"
```

**Пример ответа:**

```json
{
  "symbol": "BNBUSDT",
  "start_date": "2025-01-01",
  "end_date": "2025-01-02",
  "trend": "UPWARD"
}
```

Возможные значения поля `trend`:

| Значение | Описание |
|---|---|
| `UPWARD` | Последняя цена выше первой |
| `DOWNWARD` | Последняя цена ниже первой |
| `FLAT` | Первая и последняя цены равны |

Для определения тренда требуется как минимум две записи за выбранный период.

## Формат времени

Параметры `from` и `to` принимаются в формате ISO-8601. Рекомендуется передавать время в UTC с суффиксом `Z`:

```text
2025-01-01T00:00:00Z
```

Убедитесь, что начало периода не позже его окончания и что в выбранном интервале есть сохранённые записи.

## Обработка ошибок

- Если подходящие записи отсутствуют, сервис может выбросить `RecordsNotFoundException`.
- Допустимый номер страницы — `0` или больше.
- Размер страницы должен находиться в диапазоне от `1` до `200`.
- Для определения тренда необходимо не менее двух записей.
- Для сбора данных требуется доступ к Binance API.
- Ошибки сбора данных записываются в лог приложения.

HTTP-статусы и формат JSON для ошибок определяются классом `ApiExceptionHandler`.

## Тестирование

Запуск тестов:

**Linux / macOS:**

```bash
./gradlew test
```

**Windows:**

```powershell
.\gradlew.bat test
```

HTML-отчёт Gradle формируется по пути:

```text
build/reports/tests/test/index.html
```

## Структура проекта

```text
src/
├── main/
│   ├── java/com/example/binance_price_service/
│   │   ├── config/       # Конфигурация и свойства
│   │   ├── controller/   # REST-контроллер и обработка ошибок
│   │   ├── dto/          # DTO для запросов и ответов
│   │   ├── model/        # JPA-сущности
│   │   ├── repository/   # Доступ к базе данных
│   │   └── service/      # Бизнес-логика и сбор цен
│   └── resources/
│       └── application.yaml
└── test/
    └── java/             # Автоматические тесты
```

