# ArtReid-3

> CRM-аналитика лидов — Spring Boot REST API для отслеживания лидов и их перемещения по воронке продаж.
> **Frontend репозиторий:** [mipt-ck-hackaton-2026/artreid-3-frontend](https://github.com/mipt-ck-hackaton-2026/artreid-3-frontend)

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6db33f?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-8.14.4-02303a?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169e1?logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## 📑 Содержание

- [Требования](#-требования)
- [Быстрый старт](#-быстрый-старт)
- [Архитектура проекта](#-архитектура-проекта)
- [Структура каталогов](#-структура-каталогов)
- [База данных и миграции](#-база-данных-и-миграции)
- [Конфигурация](#-конфигурация)
- [API Документация](#-api-документация)
- [Тестирование](#-тестирование)
- [Docker](#-docker)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Semantic Release и версионирование](#-semantic-release-и-версионирование)
- [Conventional Commits](#-conventional-commits)
- [Полезные Gradle-команды](#-полезные-gradle-команды)
- [Troubleshooting](#-troubleshooting)

---

## 🔧 Требования

| Инструмент     | Версия      | Примечание                                            |
|----------------|-------------|-------------------------------------------------------|
| **JDK**        | 25          | Рекомендуется [Eclipse Temurin](https://adoptium.net) |
| **Gradle**     | 8.14.4      | Встроен через Gradle Wrapper (`./gradlew`)            |
| **PostgreSQL** | 18          | Можно запустить через Docker Compose                  |
| **Docker**     | 24+         | Опционально — для контейнеризации                     |
| **Docker Compose** | v2      | Опционально — для локального окружения                |
---

## 🚀 Быстрый старт (Запуск одной командой)

### 1. Клонируйте репозиторий

```bash
git clone https://github.com/mipt-ck-hackaton-2026/artreid-3.git
cd artreid-3
```

### 2. Запуск приложения (одна из команд на выбор)

Проект можно запустить **полностью одной командой**, как требуют правила. Выберите любой удобный вариант:

**Вариант А: Через Gradle (Локальная разработка)**
Автоматически поднимает базу данных (через Spring Boot Docker Compose) и запускает приложение:
```bash
./gradlew bootRun
```

**Вариант Б: Через Docker Compose (Запуск всего стека: БД + Приложение)**
```bash
docker-compose -f dockerfiles_prod/docker-compose.yml up --build -d
```

Приложение запустится на `http://localhost:8080`.
Frontend будет доступен на `http://localhost:3000`.

### 3. Проверьте что всё работает

```bash
curl http://localhost:8080/api/health
# {"status":"UP","version":"0.0.1-SNAPSHOT"}
```

---

## 🏗 Архитектура проекта

Проект построен по **классической слоистой архитектуре** (Layered Architecture) Spring Boot:

```
┌──────────────────────────────────────────────────┐
│                   Client (HTTP)                  │
└──────────────────────┬───────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│              Controller Layer                    │
│         (REST endpoints, валидация)              │
│         HealthController, ...                    │
└──────────────────────┬───────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│               Service Layer                      │
│       (бизнес-логика, оркестрация)               │
└──────────────────────┬───────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│             Repository Layer                     │
│     (Spring Data JPA, доступ к данным)           │
└──────────────────────┬───────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│              Model Layer                         │
│         (JPA entities, DTO)                      │
└──────────────────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────┐
│            PostgreSQL Database                   │
│     (миграции через Liquibase)                   │
└──────────────────────────────────────────────────┘
```

### Ключевые технологии

| Технология                    | Назначение                                                    |
|-------------------------------|---------------------------------------------------------------|
| **Spring Boot 3.5.13**        | Фреймворк приложения                                         |
| **Spring Web (MVC)**          | REST API контроллеры                                          |
| **Spring Data JPA**           | ORM-слой для работы с PostgreSQL                              |
| **Liquibase**                 | Версионирование и миграции схемы БД                           |
| **Lombok**                    | Сокращение boilerplate-кода (геттеры, конструкторы и т.д.)    |
| **SpringDoc OpenAPI**         | Автогенерация Swagger UI и OpenAPI-спецификации               |
| **Spring Boot DevTools**      | Hot-reload при разработке                                     |
| **Spring Boot Docker Compose**| Авто-запуск зависимых сервисов (PostgreSQL) через Docker      |

### Схема базы данных

```
┌─────────────────────────────────┐       ┌──────────────────────────────────┐
│             leads               │       │         lead_events              │
├─────────────────────────────────┤       ├──────────────────────────────────┤
│ lead_id           BIGSERIAL PK  │◄──┐  │ lead_event_id  BIGSERIAL PK     │
│ external_lead_id  VARCHAR (UQ)  │   │  │ lead_id        BIGINT NOT NULL   │──► FK → leads
│ manager_id        VARCHAR       │   │  │ stage_name     VARCHAR NOT NULL  │
│ pipeline_id       INTEGER       │   └──│ event_time     TIMESTAMP NOT NULL│
│ delivery_service  VARCHAR       │      │                                  │
│ city              VARCHAR       │      │ UQ(lead_id, stage_name)          │
│ delivery_manager_id VARCHAR     │      └──────────────────────────────────┘
│ lead_qualification  VARCHAR     │        IDX: (lead_id, event_time)
│ outcome_unknown     BOOLEAN     │        IDX: (stage_name, event_time)
│ lifecycle_incomplete BOOLEAN    │
└─────────────────────────────────┘
```

---

## 📂 Структура каталогов

```
artreid-3/
├── .github/
│   └── workflows/
│       └── ci.yml                      # CI/CD pipeline (GitHub Actions)
├── dockerfiles_prod/
│   └── docker-compose.yml              # Docker Compose для продакшена
├── gradle/
│   └── wrapper/                        # Gradle Wrapper (не удалять!)
├── src/
│   ├── main/
│   │   ├── java/com/ck/hackaton/artreid_3/artreid3/
│   │   │   ├── Artreid3Application.java       # Точка входа
│   │   │   ├── controller/                    # REST-контроллеры
│   │   │   │   ├── HealthController.java      # Проверка состояния
│   │   │   │   ├── DataLoadController.java    # Импорт CSV
│   │   │   │   ├── SlaAnalyticsController.java # Комплексная аналитика
│   │   │   │   ├── SlaConfigController.java   # Управление конфигом
│   │   │   │   └── OrderTimelineController.java # Таймлайны заказов
│   │   │   ├── service/                       # Бизнес-логика
│   │   │   │   ├── B2CSlaService.java         # Логика SLA 1-3
│   │   │   │   ├── DeliveryMetricsService.java # Логика SLA 4-5
│   │   │   │   ├── SlaService.java            # Общая агрегация
│   │   │   │   └── OrderTimelineService.java  # Построение цепочек событий
│   │   │   ├── repository/                    # Spring Data JPA
│   │   │   ├── model/                         # Сущности (Lead, LeadEvent)
│   │   │   └── config/                        # Конфигурации и Hot-reload
│   │   └── resources/
│   │       ├── application.properties         # Базовая конфигурация приложения
│   │       ├── sla-config.yml                 # Динамические настройки SLA
│   │       ├── db/changelog/
│   │       │   ├── db.changelog-master.yaml   # Мастер-файл Liquibase
│   │       │   └── changes/
│   │       │       └── 001-create-schema.sql  # Миграции БД
│   │       ├── static/                        # Статические файлы
│   │       └── templates/                     # Шаблоны
│   └── test/
│       └── java/com/ck/hackaton/artreid_3/artreid3/
│           ├── Artreid3ApplicationTests.java          # Smoke-тест контекста
│           ├── controller/                            # Интеграционные тесты API
│           │   ├── DataLoadControllerIntegrationTest.java
│           │   ├── OrderTimelineControllerIntegrationTest.java
│           │   └── SlaAnalyticsControllerIntegrationTest.java
│           ├── service/                               # Тесты бизнес-логики
│           │   └── OrderTimelineServiceIntegrationTest.java
│           └── util/                                  # Unit-тесты утилит
│               ├── DateResolutionUtilTest.java
│               └── DateValidationUtilTest.java
├── build.gradle                # Конфигурация сборки
├── settings.gradle             # Настройки Gradle (имя проекта)
├── compose.yaml                # Docker Compose для локальной разработки
├── Dockerfile                  # Multi-stage Docker-образ
├── .releaserc.json             # Конфигурация Semantic Release
├── .gitignore                  # Игнорируемые файлы
└── LICENSE                     # MIT License
```

---

## 🗃 База данных и миграции

### Liquibase

Проект использует **Liquibase** для управления миграциями базы данных. Все миграции применяются автоматически при старте приложения.

**Мастер-файл:** `src/main/resources/db/changelog/db.changelog-master.yaml`

### Как добавить новую миграцию

1. Создайте SQL-файл в `src/main/resources/db/changelog/changes/`:
   ```
   002-add-new-table.sql
   ```

2. Добавьте include в `db.changelog-master.yaml`:
   ```yaml
   databaseChangeLog:
     - include:
         file: db/changelog/changes/001-create-schema.sql
     - include:
         file: db/changelog/changes/002-add-new-table.sql
   ```

3. Нумеруйте файлы последовательно: `001-`, `002-`, `003-` и т.д.

> **⚠️ Важно:** Никогда не изменяйте уже применённые миграции! Всегда создавайте новый файл.

---

## ⚙ Конфигурация

### application.properties

Основной файл конфигурации: `src/main/resources/application.properties`

```properties
spring.application.name=artreid3

# Настройки загрузки файлов (для импорта CSV)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Переменные окружения

Для подключения к внешней БД (CI, продакшен) используются переменные окружения:

| Переменная                        | Описание                     | Пример                                     |
|-----------------------------------|------------------------------|---------------------------------------------|
| `SPRING_DATASOURCE_URL`          | JDBC URL базы данных          | `jdbc:postgresql://localhost:5432/artreid3` |
| `SPRING_DATASOURCE_USERNAME`     | Имя пользователя БД          | `artreid3`                                  |
| `SPRING_DATASOURCE_PASSWORD`     | Пароль пользователя БД       | `artreid3`                                  |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`  | Стратегия DDL (prod: validate)| `validate`                                 |

### Динамическая настройка SLA (sla-config.yml)

В проекте реализована гибкая настройка SLA через внешний YAML-файл `src/main/resources/sla-config.yml`. Это позволяет менять пороги SLA и корзины нарушений без пересборки приложения.

| Секция | Параметр | Описание |
| :--- | :--- | :--- |
| **b2c** | `reaction_minutes` | SLA-1: Время реакции менеджера |
| | `to_assembly_hours` | SLA-2: От реакции до сборки |
| | `assembly_to_delivery_days` | SLA-3: От сборки до передачи в доставку |
| **delivery** | `to_pvz_days` | SLA-4: Доставка до ПВЗ |
| | `pvz_storage_days` | SLA-5: Хранение на ПВЗ |
| **breach_buckets** | `short_minutes` | Границы для минутных SLA (напр. [15, 60]) |
| | `days` | Границы для дневных SLA (напр. [1, 3]) |

### 🚀 Hot-reload конфига

Приложение автоматически отслеживает изменения в файле конфигурации каждые **5 секунд**. 

**Приоритет поиска файла:**
1. Файл `sla-config.yml` в корневой папке запущенного приложения (внешний конфиг).
2. `src/main/resources/sla-config.yml` (внутренний конфиг по умолчанию).

> [!TIP]
> Чтобы изменить настройки "на лету" в Docker или на сервере, просто положите файл `sla-config.yml` рядом с JAR-архивом или в корень проекта. Перезапуск не требуется!

---

## 📘 API Документация

Проект интегрирован со **SpringDoc OpenAPI** — интерактивная документация доступна после запуска:

| Ресурс          | URL                                            |
|------------------|-------------------------------------------------|
| **Swagger UI**   | http://localhost:8080/swagger-ui.html           |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs               |
| **OpenAPI YAML** | http://localhost:8080/v3/api-docs.yaml          |

### Доступные эндпоинты

| Метод | Путь                               | Описание                                                                            |
|-------|------------------------------------|-------------------------------------------------------------------------------------|
| GET   | `/api/health`                      | Проверка состояния приложения и его версии                                          |
| POST  | `/api/data/load`                   | Загрузка датасета лидов из CSV-файла (используется быстрый batch-upsert до 50MB)    |
| GET   | `/api/sla/delivery/by-manager`     | Получение SLA-метрик (avg, median, p90, compliance %) по менеджерам                 |

---

## 🧪 Тестирование

### Запуск всех тестов

```bash
./gradlew test
```

Для тестов, требующих БД, предварительно поднимите PostgreSQL:

```bash
# Вариант 1: через Docker Compose
docker compose up -d

# Вариант 2: через переменные окружения (для внешней БД)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/artreid3 \
SPRING_DATASOURCE_USERNAME=artreid3 \
SPRING_DATASOURCE_PASSWORD=artreid3 \
./gradlew test
```

### Отчёт о тестах

После выполнения тестов HTML-отчёт доступен по пути:

```
build/reports/tests/test/index.html
```

Откройте файл в браузере для просмотра детальных результатов.

### Типы тестов

| Тип                | Аннотация         | Описание                                                   |
|--------------------|-------------------|------------------------------------------------------------|
| **Unit-тесты**     | `@WebMvcTest`     | Тестируют контроллеры изолированно с MockMvc                |
| **Smoke-тест**     | `@SpringBootTest` | Проверяет что Spring-контекст поднимается без ошибок        |
| **Integration**    | `@SpringBootTest` | Полноценные интеграционные тесты с БД (при необходимости)   |


### Как писать новые тесты

#### Unit-тест контроллера

```java
@WebMvcTest(MyController.class)
class MyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyService myService;  // мокаем зависимости

    @Test
    void shouldReturnData() throws Exception {
        when(myService.getData()).thenReturn(List.of("item1"));

        mockMvc.perform(get("/api/my-endpoint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("item1"));
    }
}
```

#### Интеграционный тест

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateEntity() {
        var response = restTemplate.postForEntity("/api/leads", newLead, Lead.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
```

### Используемые библиотеки для тестирования

| Библиотека            | Назначение                                            |
|-----------------------|-------------------------------------------------------|
| **JUnit 5**           | Фреймворк тестирования                               |
| **Mockito**           | Мокирование зависимостей                              |
| **MockMvc**           | Тестирование HTTP-слоя без запуска сервера             |
| **Spring Boot Test**  | Автоконфигурация тестового контекста                   |

---

## 🐳 Docker

### Dockerfile (multi-stage)

Проект использует двухэтапную сборку для минимизации размера образа:

```
Этап 1 (builder): gradle:jdk25-corretto  → сборка JAR
Этап 2 (runtime): eclipse-temurin:25-jre-alpine → запуск приложения
```

### Сборка и запуск через Docker

```bash
# Собрать образ
docker build -t artreid3:latest .

# Запустить контейнер
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/artreid3 \
  -e SPRING_DATASOURCE_USERNAME=artreid3 \
  -e SPRING_DATASOURCE_PASSWORD=artreid3 \
  artreid3:latest
```

### Docker Compose — запуск проекта одной командой

Для автоматического запуска приложения и базы данных используйте единую команду `docker-compose`:

```bash
# Запуск полного стека (приложение + БД)
docker-compose -f dockerfiles_prod/docker-compose.yml up --build -d
```
*(Поддерживается как `docker-compose`, так и `docker compose`)*

```bash
# Остановка с удалением volumes
docker-compose -f dockerfiles_prod/docker-compose.yml down -v
```

### Spring Boot Docker Compose (dev-режим)

Файл `compose.yaml` в корне содержит только PostgreSQL для локальной разработки. Но благодаря Spring Boot Docker Compose интеграции **отдельно базу поднимать не нужно**. Достаточно запустить приложение через Gradle:

```bash
# Эта команда автоматически поднимет БД в Docker и запустит приложение
./gradlew bootRun
```

**Параметры продакшен-окружения:**

| Параметр                      | Значение                              |
|-------------------------------|---------------------------------------|
| App port                      | `8080`                                |
| DB name                       | `artreid`                             |
| DB user                       | `artreid`                             |
| DB password                   | `artreid`                             |
| Hibernate DDL-auto            | `validate` (только проверка схемы)    |
| Volume                        | `postgres_data` (данные персистентны) |

---

## 🔄 CI/CD Pipeline

Пайплайн определён в `.github/workflows/ci.yml` и состоит из **4 последовательных джобов**:

```
push/PR → main
         │
         ▼
  ┌──────────────┐
  │   1. test    │  Запуск тестов (Gradle + PostgreSQL)
  └──────┬───────┘
         │ ✅
         ▼
  ┌──────────────┐
  │  2. release  │  Semantic Release (только main, не PR)
  └──────┬───────┘
         │ новая версия?
         ▼
  ┌──────────────┐     ┌───────────────────┐
  │ 3. publish   │     │ 4. publish-docker │
  │    JAR       │     │    Docker image   │
  │ → GH Packages│     │  → GHCR           │
  └──────────────┘     └───────────────────┘
```

### Джобы

| Джоб             | Триггер                        | Описание                                                             |
|------------------|--------------------------------|----------------------------------------------------------------------|
| **test**         | push / PR в `main`             | Запускает `./gradlew test` с PostgreSQL 18 в сервис-контейнере       |
| **release**      | push в `main` (не PR)          | Запускает Semantic Release, определяет новую версию по коммитам       |
| **publish-jar**  | Если release создал версию     | Собирает JAR и публикует в GitHub Packages (Maven)                   |
| **publish-docker** | Если release создал версию   | Собирает Docker-образ и пушит в GitHub Container Registry (GHCR)     |

### Артефакты CI

- **test-report** — HTML-отчёт о тестах (загружается в GitHub Actions Artifacts при каждом запуске)

---

## 🏷 Semantic Release и версионирование

Проект использует **Semantic Release** для автоматического версионирования. Конфигурация в `.releaserc.json`.

### Как это работает

1. При push в `main` Semantic Release анализирует коммиты по [Conventional Commits](https://www.conventionalcommits.org/)
2. Определяет тип версии: `patch`, `minor` или `major`
3. Обновляет `version` в `build.gradle`
4. Генерирует `CHANGELOG.md`
5. Создаёт Git-тег и GitHub Release
6. Запускает публикацию JAR и Docker-образа

### Плагины

| Плагин                            | Назначение                                    |
|-----------------------------------|-----------------------------------------------|
| `@semantic-release/commit-analyzer` | Анализ типа релиза по коммитам              |
| `@semantic-release/release-notes-generator` | Генерация release notes             |
| `@semantic-release/changelog`     | Обновление CHANGELOG.md                      |
| `@semantic-release/exec`         | Обновление версии в `build.gradle`            |
| `@semantic-release/git`          | Коммит изменённых файлов                      |
| `@semantic-release/github`       | Создание GitHub Release                       |

---

## 📝 Conventional Commits

Для корректной работы Semantic Release используйте формат [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Типы коммитов

| Тип        | Версия  | Описание                                           | Пример                                        |
|------------|---------|----------------------------------------------------|-------------------------------------------------|
| `feat`     | MINOR   | Новая функциональность                             | `feat(leads): add lead creation endpoint`       |
| `fix`      | PATCH   | Исправление бага                                   | `fix(db): correct FK constraint on lead_events` |
| `docs`     | —       | Изменения документации                             | `docs: update README with API examples`         |
| `style`    | —       | Форматирование, без изменения логики               | `style: apply code formatting`                  |
| `refactor` | —       | Рефакторинг без изменения поведения                | `refactor(service): extract lead validation`    |
| `test`     | —       | Добавление или изменение тестов                    | `test(health): add controller unit test`        |
| `chore`    | —       | Обновление зависимостей, конфигов и т.д.           | `chore: update Spring Boot to 3.5.13`           |
| `perf`     | PATCH   | Улучшения производительности                       | `perf(query): add index for lead lookup`        |

> **💥 Breaking Change:** Добавьте `BREAKING CHANGE:` в footer или `!` после типа для повышения MAJOR-версии:
> ```
> feat(api)!: change health endpoint response format
> ```

---

## 🛠 Полезные Gradle-команды

```bash
# Запуск приложения (dev-режим с hot-reload)
./gradlew bootRun

# Сборка JAR (без тестов)
./gradlew bootJar -x test

# Сборка JAR (с тестами)
./gradlew bootJar

# Только запуск тестов
./gradlew test

# Очистка артефактов сборки
./gradlew clean

# Полная пересборка
./gradlew clean build

# Публикация JAR в GitHub Packages
./gradlew publish -x test

# Просмотр дерева зависимостей
./gradlew dependencies --configuration runtimeClasspath

# Проверка версий зависимостей
./gradlew dependencyUpdates  # (если подключён плагин)
```

---

## 🔥 Troubleshooting

### Приложение не запускается — "DataSource URL not set"

PostgreSQL не запущен. Убедитесь что:
```bash
docker compose up -d
# или
docker ps  # проверьте что контейнер postgres работает
```

### Тесты падают с ошибкой подключения к БД

Для `@SpringBootTest` тестов нужна работающая БД. Задайте переменные окружения:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/artreid3
export SPRING_DATASOURCE_USERNAME=artreid3
export SPRING_DATASOURCE_PASSWORD=artreid3
./gradlew test
```

### Lombok-аннотации не работают в IDE

- **IntelliJ IDEA:** Установите плагин Lombok → `Settings → Build → Compiler → Annotation Processors → Enable`
- **VS Code:** Установите расширение "Lombok Annotations Support for VS Code"

### Ошибка "JDK 25 not found"

Установите JDK 25:
```bash
# Через SDKMAN!
sdk install java 25-tem

# Или через apt (Ubuntu)
sudo apt install openjdk-25-jdk
```

### Docker build падает — "gradle: not found"

Убедитесь что вы собираете из корня проекта:
```bash
docker build -t artreid3:latest .
```

### Liquibase — "checksum mismatch"

Кто-то отредактировал уже применённую миграцию. **Никогда** не меняйте применённые миграции. Для dev-окружения можно сбросить БД:
```bash
docker compose down -v
docker compose up -d
```

---

## 🛠 Вспомогательные скрипты (Utils)

В папке `utils/` находятся вспомогательные скрипты для работы с данными:
- `utils/header_explorer/extract_headers.py` — Python-скрипт для быстрого извлечения названий колонок (заголовков) из большого сырого массива `dataset.csv` в человекочитаемый вид без загрузки всего файла в память.

---

## 👥 Команда

MIPT CK Hackathon 2026 Team

## 📄 Лицензия

Проект лицензирован под [MIT License](LICENSE).
