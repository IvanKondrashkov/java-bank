# Front Service
Front UI сервис для взаимодействия пользователя с банковской системой.

## Описание
Front Service предоставляет веб-интерфейс для управления банковскими счетами, выполнения переводов, операций с наличными и просмотра профиля пользователя.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Web MVC** - Web Framework
- **Thymeleaf** - Template Engine
- **Spring Security OAuth2** - Авторизация через Keycloak
- **Spring Cloud OpenFeign** - HTTP клиент для взаимодействия с Gateway
- **Spring Cloud LoadBalancer** - Client-side load balancing

## Функционал
- **Управление профилем пользователя** - просмотр и редактирование данных аккаунта
- **Операции с наличными** - пополнение и снятие денег со счетов
- **Переводы между счетами** - перевод средств на другие счета
- **Просмотр счетов** - отображение всех счетов пользователя и общего баланса

## Запуск
### Локальный запуск
```bash
./gradlew :front-service:bootRun
```

### Docker
```bash
cd infra
docker-compose up front-service
```

## Порт
- **8090** - HTTP порт для веб-интерфейса

## Конфигурация
Конфигурация находится в `config-server/src/main/resources/config/front-service.yml`

### Основные настройки:
- `server.port` - Порт сервера
- `spring.security.oauth2.client.registration.bank-client` - Настройки OAuth2 клиента
- `gateway.url` - URL Gateway API

## API Endpoints (страницы и формы)
- `GET /` - Главная страница с профилем и операциями
- `POST /profile` - Создание или обновление профиля пользователя
- `POST /cash/deposit` - Пополнение счета
- `POST /cash/withdraw` - Снятие денег со счета
- `POST /transfer` - Перевод между счетами
- `POST /accounts` - Создание нового счёта

## Аутентификация
Front Service использует **OAuth 2.0 Authorization Code Flow** для аутентификации пользователей через Keycloak.
**Процесс:**
1. Пользователь открывает Front UI
2. Если не аутентифицирован, перенаправляется на Keycloak
3. После успешной авторизации получает JWT токен
4. JWT токен передается в Gateway API при каждом запросе

## Структура проекта
```
front-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/front/
│   │   │       ├── client/          # Feign клиенты
│   │   │       ├── config/          # Конфигурации
│   │   │       ├── constants/       # Константы
│   │   │       ├── controller/      # Контроллеры
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── exception/       # Обработчики исключений
│   │   │       ├── model/           # Модели
│   │   │       ├── service/         # Бизнес-логика
│   │   │       ├── util/            # Утилиты
│   │   │       └── validation/      # Валидаторы
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf шаблоны
│   │       └── application.yml      # Конфигурация
│   └── test/                        # Тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :front-service:test
```