# Config Server
Externalized Configuration сервер на Spring Cloud Config.

## Описание
Config Server хранит конфигурации для всех микросервисов в централизованном месте. Каждый микросервис получает свою конфигурацию из Config Server при запуске.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Cloud Config Server** - Externalized Configuration
- **Spring Cloud Bus** - Обновление конфигураций через RabbitMQ

## Функционал
- **Хранение конфигураций** - централизованное хранение конфигураций для всех микросервисов
- **Обновление конфигураций** - обновление конфигураций без перезапуска сервисов через Spring Cloud Bus
- **Профили** - поддержка различных профилей (default, docker, dev, prod)

## Запуск
### Локальный запуск
```bash
./gradlew :config-server:bootRun
```

### Docker
```bash
cd infra
docker-compose up config-server
```

## Порт
- **8888** - HTTP порт для Config Server API

## Конфигурации
Конфигурации хранятся в `config-server/src/main/resources/config/`:

- `application.yml` - общие настройки для всех сервисов
- `account-service.yml` - настройки Account Service
- `transfer-service.yml` - настройки Transfer Service
- `cash-service.yml` - настройки Cash Service
- `notifications-service.yml` - настройки Notifications Service
- `gateway-service.yml` - настройки Gateway Service
- `front-service.yml` - настройки Front Service

## Обновление конфигураций
### Через Spring Cloud Bus
```bash
# Обновить все сервисы
curl -X POST http://localhost:8888/actuator/busrefresh

# Обновить конкретный сервис
curl -X POST http://localhost:8082/actuator/refresh
```

## Структура проекта
```
config-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/
│   │   │       └── ConfigServerApplication.java   # главный класс
│   │   └── resources/
│   │       ├── config/              # Конфигурации микросервисов
│   │       └── application.yml      # Конфигурация Config Server
│   └── test/                        # Тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :config-server:test
```