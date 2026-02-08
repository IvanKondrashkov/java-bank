# Gateway Service
API Gateway на Spring Cloud Gateway для маршрутизации запросов к микросервисам.

## Описание
Gateway Service является единой точкой входа для всех запросов к микросервисам. Он обеспечивает маршрутизацию, балансировку нагрузки, передачу JWT токенов и CORS настройки.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Cloud Gateway** - API Gateway
- **Spring Security OAuth2** - Валидация JWT токенов
- **Spring Cloud LoadBalancer** - Client-side load balancing
- **Spring Cloud Netflix Eureka** - Service Discovery

## Функционал
- **Маршрутизация запросов** - маршрутизация запросов к соответствующим микросервисам
- **Передача JWT токенов** - автоматическая передача JWT токенов от Front Service к микросервисам
- **Валидация JWT токенов** - валидация JWT токенов через Keycloak
- **CORS настройки** - настройка CORS для Front Service
- **Load Balancing** - балансировка нагрузки между экземплярами микросервисов

## Запуск
### Локальный запуск
```bash
./gradlew :gateway-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up gateway-service
```

## Порт
- **8081** - HTTP порт для Gateway API

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/gateway-service.yml`

### Маршруты:
- `/api/accounts/**` → `account-service`
- `/api/transfers/**` → `transfer-service`
- `/api/cash/**` → `cash-service`
- `/api/notifications/**` → `notifications-service`

## Аутентификация
Gateway Service использует **OAuth 2.0 Resource Server** для валидации JWT токенов от Front Service.
**Процесс:**
1. Front Service отправляет запрос с JWT токеном пользователя
2. Gateway Service валидирует токен через Keycloak
3. После валидации передает токен дальше в микросервисы через `JwtTokenRelay` filter

## Структура проекта
```
gateway-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/
│   │   │       ├── GatewayServiceApplication.java
│   │   │       └── gateway/
│   │   │           └── config/      # Security, JwtTokenRelay
│   │   └── resources/
│   │       └── application.yml      # Конфигурация
│   └── test/                        # Тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :gateway-service:test
```