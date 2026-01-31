# Account Service
Микросервис для управления банковскими счетами.

## Описание
Account Service отвечает за управление банковскими счетами пользователей, включая создание счетов, получение информации о счетах, обновление балансов и управление профилями пользователей.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Data JPA** - Работа с БД
- **PostgreSQL** - База данных
- **Liquibase** - Миграции БД
- **Spring Security OAuth2** - Авторизация через Keycloak
- **Spring Cloud OpenFeign** - HTTP клиент для межсервисных вызовов
- **Spring Cloud LoadBalancer** - Client-side load balancing

## Функционал
- **Управление счетами** - создание, получение информации о счетах
- **Управление балансами** - обновление балансов счетов
- **Управление профилями пользователей** - создание, получение и обновление профилей
- **Интеграция с Notifications Service** - публикация уведомлений о создании счетов и изменении балансов в RabbitMQ

## Запуск
### Локальный запуск
```bash
./gradlew :account-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up account-service
```

## Порт
- **8082** - HTTP порт для REST API

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/account-service.yml`

### Основные настройки:
- `server.port` - Порт сервера
- `spring.datasource` - Настройки подключения к БД
- `spring.security.oauth2.resourceserver` - Настройки OAuth2 Resource Server
- `spring.cloud.openfeign.client.config` - Настройки Feign клиентов

## API Endpoints
Базовый путь: `/accounts` (через Gateway: `/api/accounts`).

### Accounts
- `POST /accounts` - Создание нового счета
- `GET /accounts/number/{accountNumber}` - Получение счета по номеру
- `GET /accounts/user/all` - Получение всех счетов текущего пользователя
- `PUT /accounts/{accountNumber}/balance` - Обновление баланса счета
- `PUT /accounts/{accountNumber}` - Действие со счётом (query-параметр `actions`: ACCOUNT_BLOCKED, ACCOUNT_CLOSED)

### User Profiles
- `GET /accounts/user/profile` - Профиль текущего пользователя
- `POST /accounts/user/profile` - Создание профиля пользователя
- `PUT /accounts/user/profile` - Обновление профиля пользователя

## Аутентификация
Account Service использует **OAuth 2.0 Resource Server** для валидации JWT токенов от Gateway API.
**Процесс:**
1. Gateway API передает JWT токен пользователя в Account Service
2. Account Service валидирует токен через Keycloak
3. Для межсервисных вызовов используется Client Credentials Flow

## База данных
Используется схема `svc_accounts` в PostgreSQL.

### Таблицы:
- `accounts` - Банковские счета
- `users` - Профили пользователей (UserProfile)
- `transactions` - История операций по счёту

## Межсервисные вызовы
Account Service вызывает:
- **RabbitMQ** - для публикации уведомлений (Notifications Service забирает из очереди)

## Структура проекта
```
account-service/
├── src/
│   ├── main/
│   │   ├── java/.../account/
│   │   │   ├── config/          # Security, Feign, RabbitMQ
│   │   │   ├── controller/      # REST контроллеры
│   │   │   ├── exception/       # Обработка исключений
│   │   │   ├── mapper/          # MapStruct мапперы
│   │   │   ├── model/           # JPA сущности
│   │   │   ├── repository/      # Spring Data JPA
│   │   │   ├── service/         # Бизнес-логика
│   │   │   └── util/            # Утилиты
│   │   └── resources/
│   │       ├── db/changelog/    # Liquibase миграции
│   │       └── application.yml
│   ├── contractTest/            # Контракт-тесты (producer)
│   │   ├── java/.../contract/   # Базовый класс, конфиг безопасности
│   │   └── resources/contracts/
│   └── test/                    # Unit и интеграционные тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :account-service:test
./gradlew :account-service:contractTest
```