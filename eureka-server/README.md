# Eureka Server
Service Discovery сервер на Spring Cloud Netflix Eureka.

## Описание
Eureka Server обеспечивает регистрацию и обнаружение микросервисов в системе. Все микросервисы регистрируются в Eureka и могут находить друг друга через Service Discovery.

## Технологии
- **Spring Boot 3.4.2**
- **Spring Cloud Netflix Eureka Server** - Service Discovery

## Функционал
- **Регистрация сервисов** - регистрация всех микросервисов при запуске
- **Обнаружение сервисов** - обнаружение микросервисов по имени
- **Health Checks** - мониторинг состояния сервисов
- **Dashboard** - веб-интерфейс для просмотра зарегистрированных сервисов

## Запуск
### Локальный запуск
```bash
./gradlew :eureka-server:bootRun
```

### Docker
```bash
cd infra
docker-compose up eureka-server
```

## Порт
- **8761** - HTTP порт для Eureka Server и Dashboard

## Доступ
- **Eureka Dashboard:** http://localhost:8761

## Конфигурация
Конфигурация находится в `eureka-server/src/main/resources/application.yml`

## Структура проекта
```
eureka-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/yandex/practicum/bank/
│   │   │       └── EurekaServerApplication.java   # главный класс
│   │   └── resources/
│   │       └── application.yml      # Конфигурация
│   └── test/                        # Тесты
└── Dockerfile
```

## Тестирование
```bash
./gradlew :eureka-server:test
```