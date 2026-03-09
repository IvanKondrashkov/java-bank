# Java Bank
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2024.0.0-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.5+-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2-FF7900?logo=keycloak&logoColor=white)](https://www.keycloak.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Migrations-2962FF)](https://www.liquibase.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)

Микросервисное банковское приложение на Spring Boot с использованием Spring Cloud и паттернов микросервисной архитектуры.

## Описание
Веб-приложение банка, разработанное на Spring Framework 6 (Spring Boot 3.4.2) с использованием Java 21. Приложение предоставляет функционал для управления банковскими счетами, выполнения переводов, операций с наличными и получения уведомлений.

## Архитектура
Система состоит из следующих микросервисов:
- **front-service** (порт 8090) - Front UI сервис для взаимодействия пользователя с системой
- **gateway-service** (порт 8081) - API Gateway на Spring Cloud Gateway
- **account-service** (порт 8082) - Управление банковскими счетами
- **transfer-service** (порт 8083) - Переводы между счетами
- **cash-service** (порт 8084) - Операции с наличными (пополнение/снятие)
- **notifications-service** (порт 8085) - Отправка уведомлений
- **eureka-server** (порт 8761) - Service Discovery (Eureka, Docker Compose режим)
- **config-server** (порт 8888) - Externalized Config (Docker Compose режим)
- **keycloak** (порт 8080) - OAuth 2.0 Authorization Server (Keycloak)

Для Kubernetes Service Discovery реализован через `Service`, а конфигурация — через `ConfigMaps` и `Secrets`.

## Технологии
### Backend
- **Java 21**
- **Spring Boot 3.4.2**
- **Spring Cloud 2024.0.0**
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Cloud Config** - Externalized Configuration
- **Spring Security OAuth2** - Авторизация
- **Spring Data JPA** - Работа с БД
- **Spring Web MVC** - Web Framework
- **Thymeleaf** - Template Engine

### База данных
- **PostgreSQL 15+** - Основная база данных
- **Liquibase** - Миграции БД
- **H2** - Для тестов

### Инфраструктура
- **Keycloak** - OAuth 2.0 Authorization Server
- **Apache Kafka (KRaft)** - Обмен сообщениями и Spring Cloud Bus (Docker Compose: 2 брокера; Kubernetes — отдельный пукт)
- **Docker** - Контейнеризация
- **Kubernetes (Minikube)** - Локальный кластер
- **Helm** - Пакетный менеджер и шаблонизатор для K8s
- **Jenkins** - CI/CD (Docker)
- **Gradle** - Система сборки

### Безопасность
- **OAuth 2.0 Authorization Code Flow** - Для пользовательской аутентификации
- **OAuth 2.0 Client Credentials Flow** - Для межсервисной аутентификации
- **JWT** - JSON Web Tokens для передачи идентификации

## Требования
- Java 21
- Gradle 8.5+
- Docker и Docker Compose (для запуска в контейнерах)
- PostgreSQL 15+ (для локального запуска)
- Minikube, kubectl, Helm (для Kubernetes)

## Сборка проекта
### Сборка всех модулей
```bash
./gradlew clean build
```

### Сборка конкретного модуля
```bash
./gradlew :account-service:build
```

### Создание JAR файлов
```bash
./gradlew bootJar
```

## Запуск приложения
### Быстрый старт (Docker Compose)
```bash
cd infra
docker-compose up -d
```

Проверьте статус:
```bash
docker-compose ps
```

### Kubernetes (Minikube + Helm)
#### 1. Подготовка Minikube и Ingress
```bash
minikube start --memory=15884 --cpus=4 --driver=docker
minikube addons enable ingress
```

#### 2. Сборка и публикация образов
Рекомендуемый путь — через Jenkins (см. раздел ниже). Если нужно вручную:
```bash
./gradlew test
docker build -t ghcr.io/<user>/account-service:<tag> account-service
docker build -t ghcr.io/<user>/cash-service:<tag> cash-service
docker build -t ghcr.io/<user>/transfer-service:<tag> transfer-service
docker build -t ghcr.io/<user>/notifications-service:<tag> notifications-service
docker build -t ghcr.io/<user>/gateway-service:<tag> gateway-service
docker build -t ghcr.io/<user>/front-service:<tag> front-service
```

#### 3. Установка Helm-чарта
```bash
helm dependency update infra/k8s/bank
helm upgrade --install bank infra/k8s/bank \
  --namespace dev --create-namespace \
  --set account-service.image.repository=ghcr.io/<user>/account-service \
  --set account-service.image.tag=<tag> \
  --set cash-service.image.repository=ghcr.io/<user>/cash-service \
  --set cash-service.image.tag=<tag> \
  --set transfer-service.image.repository=ghcr.io/<user>/transfer-service \
  --set transfer-service.image.tag=<tag> \
  --set notifications-service.image.repository=ghcr.io/<user>/notifications-service \
  --set notifications-service.image.tag=<tag> \
  --set gateway-service.image.repository=ghcr.io/<user>/gateway-service \
  --set gateway-service.image.tag=<tag> \
  --set front-service.image.repository=ghcr.io/<user>/front-service \
  --set front-service.image.tag=<tag> \
  --set postgresql.auth.password=<db-pass> \
  --set gateway-service.ingress.enabled=true \
  --set gateway-service.ingress.hosts[0].host=gateway.dev.local \
  --set gateway-service.ingress.hosts[0].paths[0].path=/ \
  --set gateway-service.ingress.hosts[0].paths[0].pathType=Prefix \
  --set gateway-service.frontService.url=http://front.dev.local \
  --set front-service.ingress.enabled=true \
  --set front-service.ingress.hosts[0].host=front.dev.local \
  --set front-service.ingress.hosts[0].paths[0].path=/ \
  --set front-service.ingress.hosts[0].paths[0].pathType=Prefix \
  --set global.baseKeycloakHost=auth.dev.local \
  --set keycloak.ingress.enabled=true \
  --set keycloak.ingress.hostname=auth.dev.local
```

#### 4. Добавьте hosts-записи
```
127.0.0.1 gateway.dev.local
127.0.0.1 front.dev.local
127.0.0.1 auth.dev.local
```
Если меняете домены, обновите redirect-uri и baseUrl в `infra/k8s/bank/files/k8s-bank-realm.json` и перезапустите Helm релиз. Файл подключается через `keycloak.keycloakConfigCli.configuration` в `infra/k8s/bank/values.yaml`.

#### 5. CoreDNS: rewrite для Keycloak
Чтобы микросервисы могли обращаться к Keycloak по `http://auth.dev.local` (и т.п.), этот хост должен резолвиться внутри кластера в сервис Keycloak. Настройка — один раз на кластер.

**5.1. Открыть ConfigMap CoreDNS**
```bash
kubectl apply -f infra/k8s/coredns-configmap.yaml
kubectl rollout restart deployment coredns -n kube-system
```
(Для DaemonSet: `kubectl rollout restart daemonset coredns -n kube-system`.)

#### 6. Доступ к приложению из браузера (port-forward Ingress)
На Minikube с драйвером Docker IP ноды (например, 192.168.49.2) часто недоступен с хоста. Чтобы открыть фронт по `http://front.dev.local`, пробросьте порт Ingress-контроллера на localhost (команду держите запущенной в отдельном терминале):

```bash
kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 80:80
```
После этого в браузере откройте **http://front.dev.local**.

#### 7. Helm-тесты
```bash
helm test bank -n dev
```

### Jenkins (Docker)
Файлы Jenkins находятся в `infra/jenkins`.
1. Создайте `infra/jenkins/.env` на основе `.env.example`.
2. Скопируйте kubeconfig в `infra/jenkins/jenkins_kubeconfig.yaml` и замените `server` на `https://host.docker.internal:<port>`, добавив `insecure-skip-tls-verify: true`.
3. Запустите Jenkins:
```bash
cd infra/jenkins
docker compose up -d --build
```
4. Откройте Jenkins: http://localhost:8080 и запустите Multibranch pipeline `JavaBank`.

### Локальный запуск (без Docker)
#### Быстрый старт
1. **Запустить инфраструктуру (Docker):**
   ```bash
   # PostgreSQL
   docker run -d --name bank-postgres -p 5432:5432 \
     -e POSTGRES_DB=bank_db -e POSTGRES_USER=bank_user -e POSTGRES_PASSWORD=bank_password \
     postgres:15-alpine
   
   # Apache Kafka (KRaft, один брокер для локальной разработки)
   docker run -d --name bank-kafka -p 9094:9092 \
     -e KAFKA_NODE_ID=1 \
     -e KAFKA_PROCESS_ROLES=broker,controller \
     -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9094 \
     -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
     -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
     apache/kafka:3.7.0
   
   # Keycloak
   docker run -d --name bank-keycloak -p 8080:8080 \
     -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
     -e KC_HTTP_ENABLED=true \
     -v "$(pwd)/infra/keycloak/bank-realm.json:/opt/keycloak/data/import/bank-realm.json" \
     quay.io/keycloak/keycloak:23.0 start-dev --import-realm
   ```

2. **Запустить сервисы в следующем порядке (в отдельных терминалах):**
   ```bash
   # Терминал 1: Eureka Server
   ./gradlew :eureka-server:bootRun
   
   # Терминал 2: Config Server
   ./gradlew :config-server:bootRun
   
   # Терминал 3: Account Service
   ./gradlew :account-service:bootRun
   
   # Терминал 4: Transfer Service
   ./gradlew :transfer-service:bootRun
   
   # Терминал 5: Cash Service
   ./gradlew :cash-service:bootRun
   
   # Терминал 6: Notifications Service
   ./gradlew :notifications-service:bootRun
   
   # Терминал 7: Gateway Service
   ./gradlew :gateway-service:bootRun
   
   # Терминал 8: Front Service
   ./gradlew :front-service:bootRun
   ```

3. **Проверка**
   - Eureka Dashboard: http://localhost:8761
   - Config Server: http://localhost:8888/actuator/health
   - Front UI: http://localhost:8090
   - Keycloak Admin: http://localhost:8080

### Остановка
```bash
cd infra
docker-compose down
```

## Доступ к сервисам
- **Front UI:** http://localhost:8090
- **Gateway API:** http://localhost:8081
- **Eureka Dashboard:** http://localhost:8761
- **Keycloak Admin Console:** http://localhost:8080
- **Keycloak Realm:** http://localhost:8080/realms/bank
- **Config Server:** http://localhost:8888
- **Kafka UI:** http://localhost:8086

## Аутентификация и авторизация
### Настройка OAuth 2.0 (Keycloak)
#### Автоматическая настройка (рекомендуется)
При запуске через Docker Compose realm и клиенты создаются автоматически при первом запуске Keycloak.

**Как это работает:**
1. Keycloak ищет JSON файлы в директории `/opt/keycloak/data/import/`
2. Файл `bank-realm.json` монтируется через volume из `infra/keycloak/`
3. При запуске с флагом `--import-realm` Keycloak автоматически импортирует realm и всех клиентов
4. Realm `bank` и все OAuth2 клиенты создаются автоматически

**Структура файла realm:**
- Realm: `bank`
- Клиенты:
  - `bank-front-client` (Authorization Code Flow) - для Front UI
  - `account-service-client` (Client Credentials) - для Account Service
  - `transfer-service-client` (Client Credentials) - для Transfer Service
  - `cash-service-client` (Client Credentials) - для Cash Service

**Тестовые пользователи (из bank-realm.json):**
- `admin` / `admin-password` — роли: ADMIN, USER, TRANSFER_WRITE, CASH_WRITE
- `user` / `user-password` — роли: USER, TRANSFER_WRITE, CASH_WRITE

### Процесс аутентификации
#### 1. Пользовательская аутентификация (Authorization Code Flow)
**Используется для:** Front UI → Gateway API → Микросервисы

**Процесс:**
1. Пользователь открывает Front UI (http://localhost:8090)
2. Front Service проверяет наличие JWT токена
3. Если токена нет, пользователь перенаправляется на Keycloak
4. После успешной авторизации Keycloak возвращает JWT токен
5. Front Service добавляет JWT токен в заголовок `Authorization: Bearer <token>`
6. Gateway API передает токен дальше в микросервисы
7. Микросервисы валидируют JWT токен через Keycloak

#### 2. Межсервисная аутентификация (Client Credentials Flow)
**Используется для:** Микросервис → Микросервис

**Процесс:**
1. Микросервис использует свои credentials (client-id, client-secret)
2. Отправляет запрос в Keycloak для получения JWT токена
3. Добавляет JWT токен в заголовок `Authorization: Bearer <token>`
4. Отправляет запрос в другой микросервис через Feign Client
5. Микросервис валидирует JWT токен через Keycloak

## База данных
Приложение использует PostgreSQL с разделением на схемы:
- `svc_accounts` - для Account Service (таблицы: users, accounts, transactions)
- `svc_transfers` - для Transfer Service (таблицы: transfers)
- `svc_cash` - для Cash Service (таблицы: cash_operations)
- `svc_notifications` - для Notifications Service (таблицы: notifications)

Миграции выполняются автоматически при запуске через Liquibase.

## Тестирование
### Запуск всех тестов
```bash
./gradlew test
```

### Запуск тестов конкретного модуля
```bash
./gradlew :account-service:test
```

### Контракт-тесты (Spring Cloud Contract)
```bash
# Producer: генерация и верификация стабов account-service
./gradlew :account-service:contractTest

# Публикация стабов в Maven Local (нужно для consumer-тестов)
./gradlew :account-service:publishToMavenLocal

# Consumer: тесты cash-service против стабов account-service
./gradlew :cash-service:test --tests "*AccountServiceClientContractTest*"
```

## Структура проекта
```
java-bank/
├── front-service/          # Front UI сервис
├── gateway-service/        # API Gateway
├── account-service/        # Сервис счетов
├── transfer-service/       # Сервис переводов
├── cash-service/           # Сервис наличных операций
├── notifications-service/  # Сервис уведомлений
├── eureka-server/          # Service Discovery
├── config-server/          # Externalized Config
│   └── src/main/resources/config/
│       ├── account-service.yml
│       ├── transfer-service.yml
│       ├── cash-service.yml
│       ├── notifications-service.yml
│       ├── gateway-service.yml
│       └── front-service.yml
├── build.gradle            # Корневой build.gradle
├── settings.gradle         # Настройки Gradle
└── infra/                  # Инфраструктурные файлы
    ├── docker-compose.yml  # Docker Compose конфигурация
    ├── keycloak/           # Realm конфигурация Keycloak
    ├── k8s/                # Helm-чарты для Kubernetes
    └── jenkins/            # Jenkins (Docker) и pipeline
```

## Config Server (Docker Compose)
Раздел относится только к режиму Docker Compose. В Kubernetes конфигурации поставляются через `ConfigMaps` и `Secrets`.

Config Server хранит конфигурации для каждого микросервиса отдельно:
- `account-service.yml` - настройки Account Service
- `transfer-service.yml` - настройки Transfer Service
- `cash-service.yml` - настройки Cash Service
- `notifications-service.yml` - настройки Notifications Service
- `gateway-service.yml` - настройки Gateway Service
- `front-service.yml` - настройки Front Service
- `application.yml` - общие настройки для всех сервисов

Каждый микросервис получает свою конфигурацию из Config Server при запуске.

### Обновление конфигураций в реальном времени
Приложение использует **Spring Cloud Bus** (Apache Kafka) для обновления конфигураций без перезапуска сервисов в режиме Docker Compose. В Kubernetes конфигурации обновляются через пересборку/перезапуск Helm релиза.

**Процесс обновления конфигураций:**

1. Изменить конфигурационные файлы в `config-server/src/main/resources/config/`
2. Пересобрать и перезапустить Config Server
3. Обновить конфигурации на всех сервисах:
   ```bash
   # Обновить все сервисы через Bus
   curl -X POST http://localhost:8888/actuator/busrefresh
   
   # Или обновить конкретный сервис
   curl -X POST http://localhost:8082/actuator/refresh
   ```

## Разработка
### Добавление нового микросервиса
1. Создать директорию `new-service/`
2. Добавить модуль в `settings.gradle`
3. Создать `build.gradle` с зависимостями
4. Реализовать структуру пакетов: `model`, `repository`, `service`, `controller`
5. Добавить миграции Liquibase (changelog файлы в `src/main/resources/db/changelog/`)
6. Настроить маршрутизацию в Gateway
7. Добавить конфигурацию в Config Server

### Настройка OAuth 2.0 клиента
1. Добавить клиента в Keycloak (через Admin Console или автоматически)
2. Настроить `application.yml` в Config Server с client-id и client-secret
3. Настроить scopes и grant types

## Troubleshooting
### Проблемы с подключением к БД
Проверьте, что PostgreSQL запущен и доступен:
```bash
psql -h localhost -U bank_user -d bank_db
```

### Проблемы с Eureka (Docker Compose)
Убедитесь, что Eureka Server запущен первым и доступен по адресу http://localhost:8761

### Проблемы с Keycloak
1. Убедитесь, что Keycloak запущен и доступен: http://localhost:8080
2. Проверьте, что создан Realm `bank`
3. Проверьте настройки клиентов в Keycloak
4. Для Docker Compose проверьте issuer-uri в Config Server: `http://localhost:8080/realms/bank`. Для Kubernetes проверьте `ConfigMap` нужного сервиса.

### Проблемы с аутентификацией
1. Проверьте, что realm и клиенты созданы в Keycloak
2. Проверьте client-id и client-secret в конфигурации
3. Проверьте issuer-uri (должен быть `http://localhost:8080/realms/bank`)
4. Проверьте логи сервисов на наличие ошибок валидации JWT