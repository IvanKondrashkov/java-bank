# Notifications Service
Микросервис для отправки уведомлений пользователям.

## Описание
Notifications Service отвечает за отправку уведомлений пользователям о различных событиях в банковской системе: создание счетов, изменение балансов, переводы, операции с наличными. События приходят через RabbitMQ.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Data JPA** - Работа с БД
- **PostgreSQL** - База данных
- **Liquibase** - Миграции БД
- **Spring Security OAuth2** - Авторизация через Keycloak
- **Spring Cloud LoadBalancer** - Client-side load balancing

## Функционал
- **Отправка уведомлений** - создание и отправка уведомлений пользователям о выполненных действиях:
  - Переводы: входящий (`TRANSFER_IN`), исходящий (`TRANSFER_OUT`)
  - Операции с наличными: пополнение (`DEPOSIT`), снятие (`WITHDRAWAL`)
  - Счета: создание (`ACCOUNT_CREATED`), блокировка (`ACCOUNT_BLOCKED`), закрытие (`ACCOUNT_CLOSED`), изменение баланса (`BALANCE_UPDATED`)
  - Профиль: создание (`PROFILE_CREATED`), обновление (`PROFILE_UPDATED`)
  - Канал доставки: `PUSH`, `SMS`, `EMAIL` (NotificationType)
- **Каналы отправки:**
  - Логирование (реализовано) - детальное логирование всех уведомлений
  - Сохранение в БД (реализовано) - все уведомления сохраняются в PostgreSQL
  - Email (готово к расширению) - можно добавить SMTP отправку
  - SMS (готово к расширению) - можно добавить SMS-провайдера
  - Push-уведомления (готово к расширению) - можно добавить Firebase/APNs
  - Alert (готово к расширению) - можно добавить систему мониторинга
- **История уведомлений** - хранение и получение истории уведомлений
- **Типы уведомлений** - поддержка различных типов уведомлений

## Запуск
### Локальный запуск
```bash
./gradlew :notifications-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up notifications-service
```

## Порт
- **8085** - HTTP порт для REST API

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/notifications-service.yml`

## API Endpoints
Базовый путь: `/notifications` (через Gateway: `/api/notifications`).
- `GET /notifications/{id}` - Получение уведомления по ID
- `GET /notifications/user` - Получение уведомлений текущего пользователя (из JWT)

Создание уведомлений выполняется другими сервисами (Account, Transfer, Cash) через RabbitMQ.

## Аутентификация
Notifications Service использует **OAuth 2.0 Resource Server** для валидации JWT токенов на пользовательских запросах через Gateway.
**Процесс:**
1. Пользователь отправляет запросы в Notifications Service через API Gateway
2. Notifications Service валидирует JWT через Keycloak
3. Уведомления от других сервисов приходят через RabbitMQ (без REST-вызовов)

**Требования к токену:**
- Роль `SERVICE` - для идентификации сервисного аккаунта
- Токен должен быть валидным и подписанным Keycloak

## База данных
Используется схема `svc_notifications` в PostgreSQL.

### Таблицы:
- `notifications` - Уведомления пользователей

## Межсервисные вызовы
Notifications Service получает события из RabbitMQ:
- **Account Service** - уведомления о создании счетов и изменении балансов
- **Transfer Service** - уведомления о переводах
- **Cash Service** - уведомления об операциях с наличными

## Структура проекта
```
notifications-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/notification/
│   │   │       ├── config/          # Security, RabbitMQ
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       ├── listener/       # RabbitMQ listener
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
./gradlew :notifications-service:test
```