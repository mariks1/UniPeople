# UniPeople — учебный проект по Spring Cloud

HR/учёт сотрудников, выполненный в рамках курса «Высокопроизводительные системы» (лабы 1‑3 закрыты, лаба 4 ещё в работе).  


## Стек и архитектура
- Java 21, Spring Boot 3.5, Spring Cloud 2025.0, MapStruct, Flyway, Lombok.
- Монорепозиторий с микросервисами; регистрация через Eureka, конфигурация из Config Server, вход через API Gateway.
- БД: один PostgreSQL с отдельными схемами по сервисам; миграции Flyway/R2DBC.
- Хранение файлов: MinIO (S3 API) + публикация событий в Kafka (кластер не поднят в docker-compose, см. TODO).
- OpenAPI/Swagger для каждой службы, агрегировано в Gateway.

## Сервисы
### Инфраструктура
- `eureka-server` — реестр сервисов (порт 18761).
- `config-server` — читает конфиги из сабмодуля `UniPeople-config` (порт 18888, default-label=main).
- `api-gateway` — входная точка (порт 18080), маршруты `/api/v1/**` на бизнес-сервисы, Swagger-агрегация `/swagger-ui.html`.

### Бизнес-сервисы
- `auth-service` — логин по паролю, выдача JWT (RS256), JWKS по `/auth/.well-known/jwks.json`. CRUD пользователей и ролей, сидап супервайзер `supervisor/qwerty`. Роли и управляемые департаменты пишутся в клеймы.
- `organization-service` — справочники факультетов, кафедр, должностей; назначение руководителя кафедры; HEAD-эндпоинты для быстрых проверок. Feign + Resilience4j к employee-service.
- `employee-service` — сотрудники (CRUD, fire/activate), пагинация + бесконечный скролл `/api/v1/employees/stream`, HEAD для проверки существования. Проверяет департаменты через organization-service.
- `employment-service` — трудоустройства (нанять/обновить/закрыть), выборки по сотруднику и департаменту с `X-Total-Count`. WebFlux-контроллеры + JPA, Feign к org/employee.
- `duty-service` — справочник обязанностей и назначения на сотрудников кафедры с постраничкой, контроль прав через роли/managedDeptIds.
- `leave-service` — WebFlux + R2DBC: типы отпусков и заявки (draft/pending/approve/reject/cancel), выборки с `X-Total-Count`, загрузка вложений через file-service.
- `file-service` — WebFlux + R2DBC + MinIO: загрузка/скачивание/удаление файлов, постраничка (`X-Total-Count`) и бесконечный скролл `/stream`. Публикует Kafka-события `file.events`.

## Конфигурация
- Профиль по умолчанию — `dev` (см. `docker-compose.yml`): Postgres `unipeople` / `postgres:postgres`, схемы `auth`, `employee`, `org`, `employment`, `duty`, `leave`, `file`.
- MinIO: `http://localhost:19000` (консоль `:19001`), `minio_root/minio_password`, бакет `unipeople-files` создаётся автоматически.
- JWT: issuer `https://auth.unipeople`, audience `unipeople-api`; в `dev` ключи генерируются при старте.

## Запуск через Docker Compose
```bash
docker compose up --build
```
- Gateway: http://localhost:18080  
  Swagger: http://localhost:18080/swagger-ui.html (Auth/Employee/Org/Employment/Duty/Leave/File)  
- Eureka: http://localhost:18761  
- Config Server: http://localhost:18888  
- MinIO S3: http://localhost:19000 (консоль http://localhost:19001)

## Авторизация и роли
- Получить токен: `POST /auth/login` (через Gateway) с `username/password` → `TokenDto{accessToken, expiresAt}`.
- Передавать в `Authorization: Bearer <token>`.
- Основные роли в проверках SpEL: `SUPERVISOR`, `ORG_ADMIN`, `HR`, `DEPT_HEAD`; для проверки «своих» данных используются клеймы `employeeId` и `managedDeptIds`.

## TODO
- Лабораторная 4: нет Kafka/RabbitMQ кластера и сервиса уведомлений/вебсокетов; file-service уже публикует события, но брокер отсутствует в compose.
