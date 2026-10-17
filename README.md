[English](README.md) | [ไทย](README.th.md)

# Todolist

A single-user task list exposed as a JSON REST API: create Tasks, mark them completed, and remove them.

Built as a learning project on **Spring Boot 4.1.1** and **Java 25**, backed by an embedded **H2** database, with interactive API docs via **Swagger UI**.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Requirements](#requirements)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [API reference](#api-reference)
  - [Task representation](#task-representation)
  - [Endpoints](#endpoints)
  - [Create a task](#post-tasks)
  - [List tasks](#get-tasks)
  - [Get a task](#get-tasks-1)
  - [Update a task](#patch-tasksid)
  - [Delete a task](#delete-tasksid)
  - [Errors](#errors)
- [Interactive docs (Swagger UI)](#interactive-docs-swagger-ui)
- [Sample data](#sample-data)
- [Data and persistence](#data-and-persistence)
- [Project structure](#project-structure)
- [Testing](#testing)
- [Domain language](#domain-language)
- [Troubleshooting](#troubleshooting)

---

## Features

- **Create, read, update, delete** Tasks over a REST API.
- **Complete / uncomplete** a Task (no partial state).
- **Filter** by status: `all`, `active`, `completed`.
- **Newest first** listing (by `createdAt`).
- **Request validation** with clear `400` responses.
- **RFC 7807 Problem Details** (`application/problem+json`) for all errors.
- **Interactive documentation** generated automatically with springdoc-openapi.
- **Persistent storage** in a local H2 file database.
- **Optional sample data** seeded on startup.
- **Integration tests** driven through the HTTP layer.

---

## Tech stack

| Component       | Version / choice                          |
| --------------- | ----------------------------------------- |
| Language        | Java 25                                   |
| Framework       | Spring Boot 4.1.1 (Spring Framework 7)    |
| Web             | Spring Web MVC (`spring-boot-starter-webmvc`) |
| Persistence     | Spring Data JPA + Hibernate ORM           |
| Database        | H2 (file-based, embedded)                 |
| Validation      | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| API docs        | springdoc-openapi 3.1.1 + Swagger UI      |
| Build           | Maven (wrapper included)                  |
| Testing         | JUnit 5 + MockMvc (`spring-boot-starter-webmvc-test`) |

---

## Requirements

- **JDK 25** or newer.
- No standalone Maven installation needed — the Maven wrapper (`./mvnw`) is included.
- No database server needed — H2 runs embedded.

Check your environment:

```bash
java -version     # should report 25.x
```

---

## Getting started

Start the application:

```bash
./mvnw spring-boot:run
```

The API is served at **http://localhost:8080**.

Quick smoke test:

```bash
curl http://localhost:8080/tasks
```

Build a runnable jar:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## Configuration

Main settings live in `src/main/resources/application.properties`:

| Property                        | Default                              | Purpose                                    |
| ------------------------------- | ------------------------------------ | ------------------------------------------ |
| `spring.application.name`       | `todolist`                           | Application name.                          |
| `spring.datasource.url`         | `jdbc:h2:file:./data/todolist;DB_CLOSE_ON_EXIT=FALSE` | H2 file database location. |
| `spring.datasource.username`    | `sa`                                 | Database user.                             |
| `spring.datasource.password`    | *(empty)*                            | Database password.                         |
| `spring.jpa.hibernate.ddl-auto` | `update`                             | Create/update the schema from entities.    |
| `todolist.seed-sample-data`     | `true`                               | Insert 5 sample Tasks on startup if empty. |

Tests use `src/test/resources/application.properties`, which overrides the datasource with an **in-memory** H2 database and `ddl-auto=create-drop`, so tests never touch the file database.

To run without sample data, start with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--todolist.seed-sample-data=false
```

---

## API reference

Base URL: `http://localhost:8080`

### Task representation

A **Task** is returned as JSON:

```json
{
  "id": 1,
  "title": "Buy milk",
  "completed": false,
  "createdAt": "2026-10-17T16:40:34.714212Z"
}
```

| Field       | Type      | Notes                                        |
| ----------- | --------- | -------------------------------------------- |
| `id`        | integer   | Generated identifier (`Long`, auto-increment). |
| `title`     | string    | Required, must not be blank.                 |
| `completed` | boolean   | `false` on creation.                         |
| `createdAt` | string    | ISO-8601 UTC instant, immutable.             |

### Endpoints

| Method   | Path                                  | Success        | Errors            |
| -------- | ------------------------------------- | -------------- | ----------------- |
| `POST`   | `/tasks`                              | `201 Created`  | `400`             |
| `GET`    | `/tasks?status=all\|active\|completed` | `200 OK`      | —                 |
| `GET`    | `/tasks/{id}`                         | `200 OK`       | `404`             |
| `PATCH`  | `/tasks/{id}`                         | `200 OK`       | `400`, `404`      |
| `DELETE` | `/tasks/{id}`                         | `204 No Content` | `404`           |

### POST /tasks

Create a new Task. `title` is required and must not be blank.

```bash
curl -i -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title": "Buy milk"}'
```

Response — `201 Created`:

```
Location: /tasks/1
```
```json
{
  "id": 1,
  "title": "Buy milk",
  "completed": false,
  "createdAt": "2026-10-17T16:40:34.714212Z"
}
```

A blank or missing title returns `400 Bad Request`.

### GET /tasks

List all Tasks, newest first. Optionally filter with the `status` query parameter:

| `status` value | Returns                        |
| -------------- | ------------------------------ |
| `all` (default)| Every Task.                    |
| `active`       | Tasks with `completed = false`. |
| `completed`    | Tasks with `completed = true`.  |

```bash
curl http://localhost:8080/tasks
curl "http://localhost:8080/tasks?status=active"
curl "http://localhost:8080/tasks?status=completed"
```

Response — `200 OK`:

```json
[
  { "id": 2, "title": "Walk dog", "completed": false, "createdAt": "2026-10-17T16:41:00.101000Z" },
  { "id": 1, "title": "Buy milk", "completed": false, "createdAt": "2026-10-17T16:40:34.714212Z" }
]
```

### GET /tasks/{id}

Fetch a single Task.

```bash
curl http://localhost:8080/tasks/1
```

- `200 OK` with the Task when it exists.
- `404 Not Found` otherwise.

### PATCH /tasks/{id}

Partially update a Task. Send any of the mutable fields; omitted fields are left unchanged. The operation is **idempotent**.

| Field       | Type    | Notes                       |
| ----------- | ------- | --------------------------- |
| `title`     | string  | New title.                  |
| `completed` | boolean | Mark completed / uncompleted. |

```bash
# Rename
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"title": "Buy oat milk"}'

# Complete
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"completed": true}'

# Uncomplete
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"completed": false}'
```

Response — `200 OK` with the updated Task. Unknown `id` returns `404 Not Found`.

### DELETE /tasks/{id}

Remove a Task permanently.

```bash
curl -i -X DELETE http://localhost:8080/tasks/1
```

- `204 No Content` on success.
- `404 Not Found` if the Task does not exist.

### Errors

All errors use **RFC 7807 Problem Details** (`Content-Type: application/problem+json`).

`404 Not Found` (unknown Task):

```json
{
  "detail": "Task 999 not found",
  "instance": "/tasks/999",
  "status": 404,
  "title": "Not Found"
}
```

`400 Bad Request` (validation failure, e.g. blank title):

```json
{
  "detail": "Validation failed",
  "instance": "/tasks",
  "status": 400,
  "title": "Bad Request"
}
```

---

## Interactive docs (Swagger UI)

Once the app is running:

| Resource          | URL                                              |
| ----------------- | ------------------------------------------------ |
| Swagger UI        | http://localhost:8080/swagger-ui/index.html      |
| OpenAPI JSON      | http://localhost:8080/v3/api-docs                |
| OpenAPI YAML      | http://localhost:8080/v3/api-docs.yaml           |

`/swagger-ui.html` redirects (`302`) to the Swagger UI page.

---

## Sample data

On startup, if the `tasks` table is **empty**, the application seeds **5 sample Tasks**:

| Title                        | Completed |
| ---------------------------- | --------- |
| Buy groceries                | no        |
| Walk the dog                 | no        |
| Read a chapter of a book     | yes       |
| Call mom                     | yes       |
| Write the report             | no        |

Seeding is controlled by `todolist.seed-sample-data` (enabled by default). The empty-table check means restarting never duplicates data. Set the property to `false` to start with an empty list.

---

## Data and persistence

- Data is stored in an embedded **H2 file database** at `./data/todolist.mv.db` (relative to the working directory).
- The `data/` directory is **git-ignored** and created automatically on first run.
- The schema is managed by Hibernate (`ddl-auto=update`) — no migration tool is used.
- **Reset all data:** stop the application and delete the `data/` directory:

  ```bash
  rm -rf data
  ```

> **Single-instance note:** the H2 file database can be opened by only one process at a time. If an old instance still holds the file, a new one fails to start with `Unable to determine Dialect without JDBC metadata`. Stop the previous instance before starting a new one.

---

## Project structure

```
.
├── CONTEXT.md                         # Domain glossary
├── pom.xml                            # Maven build
├── mvnw / mvnw.cmd                    # Maven wrapper
├── .mvn/wrapper/maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java/com/todolist
│   │   │   ├── TodolistApplication.java     # Spring Boot entry point
│   │   │   └── task                         # Task feature
│   │   │       ├── Task.java                # JPA entity
│   │   │       ├── TaskRepository.java      # Spring Data repository
│   │   │       ├── TaskService.java         # Business logic
│   │   │       ├── TaskController.java      # REST endpoints
│   │   │       ├── CreateTaskRequest.java   # Input DTO (create)
│   │   │       ├── UpdateTaskRequest.java   # Input DTO (patch)
│   │   │       ├── TaskResponse.java        # Output DTO
│   │   │       ├── TaskNotFoundException.java
│   │   │       ├── ApiExceptionHandler.java # RFC 7807 error handling
│   │   │       └── SampleDataConfiguration.java  # Startup seed data
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java/com/todolist
│       │   ├── OpenApiDocsTest.java
│       │   └── task
│       │       ├── TaskApiTest.java
│       │       └── SampleDataTest.java
│       └── resources
│           └── application.properties       # In-memory H2 for tests
└── data/                              # Generated at runtime (git-ignored)
```

The code is layered **Controller → Service → Repository**. JPA entities are never exposed directly; requests and responses use DTOs.

---

## Testing

Run the full suite:

```bash
./mvnw test
```

- **16 integration tests** exercise the HTTP surface through `MockMvc` (create, validation, list/filter/sort, get, patch, delete, not-found, OpenAPI, sample data).
- Tests run against an **in-memory H2** database (`ddl-auto=create-drop`), isolated from your local data.
- Each API test is transactional, so tests do not leak state into one another.

Build without running tests:

```bash
./mvnw clean package -DskipTests
```

---

## Domain language

The project keeps a short domain glossary in [`CONTEXT.md`](CONTEXT.md). The two core terms:

- **Task** — a single piece of work to be done, tracked in the list. (Avoid: *Todo, item, note, entry*.)
- **Completed** — the state of a Task whose work is done; a Task is either completed or not.

---

## Troubleshooting

**Port 8080 already in use**

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN     # find the process
kill <PID>                            # stop it
```

Or start on another port:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

**`Unable to determine Dialect without JDBC metadata` on startup**

An older instance still holds the H2 file lock. Stop any running instance first, then start again.

**Database looks out of date**

Delete the `data/` directory and restart to recreate the schema and (optionally) reseed.

---

## License

No license has been declared for this project.
