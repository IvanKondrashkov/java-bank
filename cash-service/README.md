# Cash Service
Микросервис обналичивания денег для пополнения счёта или снятия денег со счёта.

## Описание
Cash Service осуществляет пополнение счёта или снятие денег со счёта. Он авторизуется на OAuth 2.0 по Client Credentials Flow для осуществления запросов в другие микросервисы. В JWT-токене содержатся привилегии для доступа в сервисы Accounts и Notifications.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Data JPA** - Работа с БД
- **PostgreSQL** - База данных
- **Liquibase** - Миграции БД
- **Spring Security OAuth2** - Авторизация через Keycloak
- **Spring Cloud OpenFeign** - HTTP клиент для межсервисных вызовов
- **Spring Cloud LoadBalancer** - Client-side load balancing

## Функционал
- **Пополнение счетов** - внесение денег на счет
- **Снятие денег** - снятие денег со счета
- **Валидация операций** - проверка достаточности средств и валидности счетов
- **История операций** - хранение и получение истории операций
- **Интеграция с Account Service** - обновление балансов счетов
- **Интеграция с Notifications Service** - публикация уведомлений об операциях в RabbitMQ

## Запуск
### Локальный запуск
```bash
./gradlew :cash-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up cash-service
```

## Порт
- **8084** - HTTP порт для REST API

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/cash-service.yml`

## API Endpoints
Базовый путь: `/cash` (через Gateway: `/api/cash`).
- `POST /cash/operations` - Выполнение операции с наличными (DEPOSIT/WITHDRAWAL)
- `GET /cash/operations/{id}` - Получение операции по ID
- `GET /cash/operations/account/{accountNumber}` - Получение операций по номеру счёта

## Аутентификация
Cash Service использует **OAuth 2.0 Client Credentials Flow** для авторизации при запросах в другие микросервисы.
**Процесс:**
1. Cash Service получает JWT токен через Client Credentials Flow от Keycloak
2. JWT токен содержит привилегии для доступа в сервисы Accounts и Notifications:
   - Роль `SERVICE` - для идентификации сервисного аккаунта
   - Роль `ACCOUNTS_WRITE` - для изменения балансов в Accounts Service
3. Токен автоматически добавляется в заголовок `Authorization: Bearer <token>` при межсервисных вызовах

**Конфигурация:**
- Client ID: `cash-service-client`
- Client Secret: `cash-service-secret`
- Grant Type: `client_credentials`
- Роли в токене: `SERVICE`, `ACCOUNTS_WRITE`

## База данных
Используется схема `svc_cash` в PostgreSQL.

### Таблицы:
- `cash_operations` - Операции с наличными

## Межсервисные вызовы
### Фронт → Cash Service
Фронт выполняет REST-запросы (в формате JSON) из блока внесения и снятия виртуальных денег в сервис Cash:
- `POST /api/cash/operations` - выполнение операции (DEPOSIT/WITHDRAWAL)

### Cash Service → Account Service
Cash выполняет REST-запросы (в формате JSON) в Accounts для внесения и снятия виртуальных денег:
- `PUT /accounts/{accountNumber}/balance` - обновление баланса счета

### Cash Service → Notifications Service
Cash публикует уведомления в RabbitMQ, которые обрабатывает Notifications Service.

## Структура проекта
```
cash-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/cash/
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
./gradlew :cash-service:test
```

Контракт-тесты (consumer) проверяют совместимость с API account-service; перед запуском нужны стабы:
```bash
./gradlew :account-service:publishToMavenLocal
```