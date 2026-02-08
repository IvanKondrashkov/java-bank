# Transfer Service
Сервис перевода денег между счетами для осуществления переводов между счетами разных пользователей.

## Описание
Transfer Service осуществляет перевод денег между счетами разных пользователей. Он авторизуется на OAuth 2.0 по Client Credentials Flow для осуществления запросов в другие микросервисы. В JWT-токене содержатся привилегии для доступа в сервисы Accounts и Notifications.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Data JPA** - Работа с БД
- **PostgreSQL** - База данных
- **Liquibase** - Миграции БД
- **Spring Security OAuth2** - Авторизация через Keycloak
- **Spring Cloud OpenFeign** - HTTP клиент для межсервисных вызовов
- **Spring Cloud LoadBalancer** - Client-side load balancing

## Функционал
- **Выполнение переводов** - перевод средств между счетами
- **Валидация переводов** - проверка достаточности средств и валидности счетов
- **История переводов** - хранение и получение истории переводов
- **Интеграция с Account Service** - обновление балансов счетов
- **Интеграция с Notifications Service** - публикация уведомлений о переводах в RabbitMQ

## Запуск
### Локальный запуск
```bash
./gradlew :transfer-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up transfer-service
```

## Порт
- **8083** - HTTP порт для REST API

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/transfer-service.yml`

## API Endpoints
Базовый путь: `/transfers` (через Gateway: `/api/transfers`).
- `POST /transfers` - Создание нового перевода
- `GET /transfers/{id}` - Получение перевода по ID
- `GET /transfers/account/{accountNumber}` - Получение переводов по номеру счёта (query-параметр `type`: TRANSFER_IN / TRANSFER_OUT)

## Аутентификация
Transfer Service использует **OAuth 2.0 Client Credentials Flow** для авторизации при запросах в другие микросервисы.
**Процесс:**
1. Transfer Service получает JWT токен через Client Credentials Flow от Keycloak
2. JWT токен содержит привилегии для доступа в сервисы Accounts и Notifications:
   - Роль `SERVICE` - для идентификации сервисного аккаунта
   - Роль `ACCOUNTS_WRITE` - для изменения балансов в Accounts Service
3. Токен автоматически добавляется в заголовок `Authorization: Bearer <token>` при межсервисных вызовах

**Конфигурация:**
- Client ID: `transfer-service-client`
- Client Secret: `transfer-service-secret`
- Grant Type: `client_credentials`
- Роли в токене: `SERVICE`, `ACCOUNTS_WRITE`

## База данных
Используется схема `svc_transfers` в PostgreSQL.

### Таблицы:
- `transfers` - Переводы между счетами

## Межсервисные вызовы
### Фронт → Transfer Service
Фронт выполняет REST-запросы (в формате JSON) из блока перевода денег в сервис Transfer:
- `POST /api/transfers` - создание нового перевода

### Transfer Service → Account Service
Transfer выполняет REST-запросы (в формате JSON) в Accounts для снятия виртуальных денег со счёта текущего пользователя и внесения на счёт другого пользователя:
- `PUT /accounts/{accountNumber}/balance` - обновление баланса счета (дебет/кредит)

### Transfer Service → Notifications Service
Transfer публикует уведомления в RabbitMQ, которые обрабатывает Notifications Service.

## Структура проекта
```
transfer-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/transfer/
│   │   │       ├── client/          # Feign (AccountServiceClient)
│   │   │       ├── config/          # Security, Feign, RabbitMQ
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       ├── mapper/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       ├── db/changelog/        # Liquibase миграции
│   │       └── application.yml      # Конфигурация
│   └── test/                        # Тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :transfer-service:test
```

Контракт-тесты (consumer) проверяют совместимость с API account-service; перед запуском нужны стабы:
```bash
./gradlew :account-service:publishToMavenLocal
```