[English](README.md) | [ไทย](README.th.md)

# Todolist

ระบบรายการงาน (task list) แบบผู้ใช้คนเดียว เปิดให้ใช้งานผ่าน JSON REST API: สร้าง Task, ทำเครื่องหมายว่าเสร็จ, และลบ Task

โปรเจกต์นี้สร้างขึ้นเพื่อการเรียนรู้บน **Spring Boot 4.1.1** และ **Java 25** ใช้ฐานข้อมูล **H2** แบบฝังในแอป พร้อมเอกสาร API แบบอินเทอร์แอกทีฟผ่าน **Swagger UI**

---

## สารบัญ

- [คุณสมบัติ](#คุณสมบัติ)
- [เทคโนโลยีที่ใช้](#เทคโนโลยีที่ใช้)
- [สิ่งที่ต้องมี](#สิ่งที่ต้องมี)
- [เริ่มต้นใช้งาน](#เริ่มต้นใช้งาน)
- [การตั้งค่า](#การตั้งค่า)
- [API Reference](#api-reference)
  - [รูปแบบของ Task](#รูปแบบของ-task)
  - [Endpoints](#endpoints)
  - [POST /tasks](#post-tasks)
  - [GET /tasks](#get-tasks)
  - [GET /tasks/{id}](#get-tasks-1)
  - [PATCH /tasks/{id}](#patch-tasksid)
  - [DELETE /tasks/{id}](#delete-tasksid)
  - [Errors](#errors)
- [Swagger UI](#swagger-ui)
- [ข้อมูลตัวอย่าง](#ข้อมูลตัวอย่าง)
- [ข้อมูลและการจัดเก็บ](#ข้อมูลและการจัดเก็บ)
- [โครงสร้างโปรเจกต์](#โครงสร้างโปรเจกต์)
- [การทดสอบ](#การทดสอบ)
- [ภาษาของโดเมน](#ภาษาของโดเมน)
- [แก้ปัญหา](#แก้ปัญหา)

---

## คุณสมบัติ

- **สร้าง อ่าน แก้ไข ลบ** Task ผ่าน REST API
- **ทำเครื่องหมายเสร็จ / ยกเลิกเสร็จ** (ไม่มีสถานะกึ่งกลาง)
- **กรองตามสถานะ**: `all`, `active`, `completed`
- **เรียงใหม่สุดก่อน** (ตาม `createdAt`)
- **ตรวจสอบข้อมูลนำเข้า** และตอบกลับ `400` อย่างชัดเจน
- **RFC 7807 Problem Details** (`application/problem+json`) สำหรับทุก error
- **เอกสารแบบอินเทอร์แอกทีฟ** สร้างอัตโนมัติด้วย springdoc-openapi
- **จัดเก็บถาวร** ในฐานข้อมูล H2 แบบไฟล์ในเครื่อง
- **ข้อมูลตัวอย่าง** (ไม่บังคับ) seed ให้ตอนเริ่มระบบ
- **Integration test** ที่ทดสอบผ่านชั้น HTTP

---

## เทคโนโลยีที่ใช้

| ส่วนประกอบ        | เวอร์ชัน / ที่เลือกใช้                       |
| ---------------- | ------------------------------------------ |
| ภาษา             | Java 25                                    |
| เฟรมเวิร์ก         | Spring Boot 4.1.1 (Spring Framework 7)     |
| เว็บ               | Spring Web MVC (`spring-boot-starter-webmvc`) |
| ชั้นจัดเก็บข้อมูล    | Spring Data JPA + Hibernate ORM            |
| ฐานข้อมูล          | H2 (แบบไฟล์ ฝังในแอป)                        |
| Validation       | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| เอกสาร API        | springdoc-openapi 3.1.1 + Swagger UI       |
| Build            | Maven (มี wrapper แนบมา)                    |
| การทดสอบ          | JUnit 5 + MockMvc (`spring-boot-starter-webmvc-test`) |

---

## สิ่งที่ต้องมี

- **JDK 25** หรือใหม่กว่า
- ไม่ต้องติดตั้ง Maven แยก เพราะมี Maven wrapper (`./mvnw`) แนบมาให้แล้ว
- ไม่ต้องมีเซิร์ฟเวอร์ฐานข้อมูล เพราะ H2 ทำงานแบบฝังในแอป

ตรวจสอบสภาพแวดล้อม:

```bash
java -version     # ควรแสดง 25.x
```

---

## เริ่มต้นใช้งาน

เริ่มแอป:

```bash
./mvnw spring-boot:run
```

API จะให้บริการที่ **http://localhost:8080**

ทดสอบเร็วๆ:

```bash
curl http://localhost:8080/tasks
```

สร้างไฟล์ jar ที่รันได้:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## การตั้งค่า

ค่าหลักอยู่ใน `src/main/resources/application.properties`:

| Property                        | ค่าเริ่มต้น                             | หน้าที่                                      |
| ------------------------------- | -------------------------------------- | -------------------------------------------- |
| `spring.application.name`       | `todolist`                             | ชื่อแอปพลิเคชัน                                |
| `spring.datasource.url`         | `jdbc:h2:file:./data/todolist;DB_CLOSE_ON_EXIT=FALSE` | ตำแหน่งฐานข้อมูล H2 แบบไฟล์ |
| `spring.datasource.username`    | `sa`                                   | ผู้ใช้ฐานข้อมูล                                |
| `spring.datasource.password`    | *(ว่าง)*                                | รหัสผ่านฐานข้อมูล                              |
| `spring.jpa.hibernate.ddl-auto` | `update`                               | สร้าง/อัปเดต schema จาก entity                |
| `todolist.seed-sample-data`     | `true`                                 | ใส่ Task ตัวอย่าง 5 รายการตอนเริ่มระบบ ถ้าตารางว่าง |

การทดสอบใช้ `src/test/resources/application.properties` ซึ่งเปลี่ยน datasource เป็น H2 **in-memory** และ `ddl-auto=create-drop` ทำให้เทสไม่แตะฐานข้อมูลไฟล์จริง

ถ้าต้องการรันโดยไม่มีข้อมูลตัวอย่าง:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--todolist.seed-sample-data=false
```

---

## API Reference

Base URL: `http://localhost:8080`

### รูปแบบของ Task

**Task** จะถูกส่งกลับเป็น JSON:

```json
{
  "id": 1,
  "title": "Buy milk",
  "completed": false,
  "createdAt": "2026-10-17T16:40:34.714212Z"
}
```

| ฟิลด์        | ชนิด      | หมายเหตุ                                     |
| ----------- | --------- | -------------------------------------------- |
| `id`        | integer   | รหัสที่สร้างอัตโนมัติ (`Long`, เพิ่มทีละหนึ่ง)  |
| `title`     | string    | จำเป็น ห้ามเว้นว่าง                            |
| `completed` | boolean   | เป็น `false` ตอนสร้าง                          |
| `createdAt` | string    | เวลา ISO-8601 UTC แก้ไขไม่ได้                   |

### Endpoints

| Method   | Path                                    | สำเร็จ            | Errors         |
| -------- | --------------------------------------- | ----------------- | -------------- |
| `POST`   | `/tasks`                                | `201 Created`     | `400`          |
| `GET`    | `/tasks?status=all\|active\|completed`  | `200 OK`          | —              |
| `GET`    | `/tasks/{id}`                           | `200 OK`          | `404`          |
| `PATCH`  | `/tasks/{id}`                           | `200 OK`          | `400`, `404`   |
| `DELETE` | `/tasks/{id}`                           | `204 No Content`  | `404`          |

### POST /tasks

สร้าง Task ใหม่ `title` จำเป็นและห้ามเว้นว่าง

```bash
curl -i -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title": "Buy milk"}'
```

ตอบกลับ — `201 Created`:

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

ถ้า `title` ว่างหรือไม่ส่งมาจะได้ `400 Bad Request`

### GET /tasks

แสดง Task ทั้งหมด เรียงใหม่สุดก่อน กรองได้ด้วย query parameter `status`:

| ค่า `status`       | ส่งกลับ                              |
| ------------------ | ----------------------------------- |
| `all` (ค่าเริ่มต้น) | ทุก Task                            |
| `active`           | Task ที่ `completed = false`        |
| `completed`        | Task ที่ `completed = true`         |

```bash
curl http://localhost:8080/tasks
curl "http://localhost:8080/tasks?status=active"
curl "http://localhost:8080/tasks?status=completed"
```

ตอบกลับ — `200 OK`:

```json
[
  { "id": 2, "title": "Walk dog", "completed": false, "createdAt": "2026-10-17T16:41:00.101000Z" },
  { "id": 1, "title": "Buy milk", "completed": false, "createdAt": "2026-10-17T16:40:34.714212Z" }
]
```

### GET /tasks/{id}

ดึง Task เดียว

```bash
curl http://localhost:8080/tasks/1
```

- `200 OK` พร้อมข้อมูล Task ถ้ามีอยู่
- `404 Not Found` ถ้าไม่มี

### PATCH /tasks/{id}

แก้ไข Task บางส่วน ส่งเฉพาะฟิลด์ที่ต้องการเปลี่ยน ฟิลด์ที่ไม่ส่งจะคงเดิม การทำงานเป็นแบบ **idempotent**

| ฟิลด์        | ชนิด    | หมายเหตุ                        |
| ----------- | ------- | ------------------------------ |
| `title`     | string  | ชื่อใหม่                         |
| `completed` | boolean | ทำเครื่องหมายเสร็จ / ยกเลิกเสร็จ     |

```bash
# เปลี่ยนชื่อ
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"title": "Buy oat milk"}'

# ทำเครื่องหมายเสร็จ
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"completed": true}'

# ยกเลิกเสร็จ
curl -X PATCH http://localhost:8080/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"completed": false}'
```

ตอบกลับ — `200 OK` พร้อม Task ที่อัปเดตแล้ว ถ้า `id` ไม่มีจะได้ `404 Not Found`

### DELETE /tasks/{id}

ลบ Task อย่างถาวร

```bash
curl -i -X DELETE http://localhost:8080/tasks/1
```

- `204 No Content` เมื่อสำเร็จ
- `404 Not Found` ถ้า Task ไม่มีอยู่

### Errors

ทุก error ใช้รูปแบบ **RFC 7807 Problem Details** (`Content-Type: application/problem+json`)

`404 Not Found` (ไม่พบ Task):

```json
{
  "detail": "Task 999 not found",
  "instance": "/tasks/999",
  "status": 404,
  "title": "Not Found"
}
```

`400 Bad Request` (validation ไม่ผ่าน เช่น title ว่าง):

```json
{
  "detail": "Validation failed",
  "instance": "/tasks",
  "status": 400,
  "title": "Bad Request"
}
```

---

## Swagger UI

เมื่อแอปทำงานอยู่:

| ทรัพยากร          | URL                                              |
| ----------------- | ------------------------------------------------ |
| Swagger UI        | http://localhost:8080/swagger-ui/index.html      |
| OpenAPI JSON      | http://localhost:8080/v3/api-docs                |
| OpenAPI YAML      | http://localhost:8080/v3/api-docs.yaml           |

`/swagger-ui.html` จะ redirect (`302`) ไปยังหน้า Swagger UI

---

## ข้อมูลตัวอย่าง

ตอนเริ่มระบบ ถ้าตาราง `tasks` **ว่าง** แอปจะ seed **Task ตัวอย่าง 5 รายการ**:

| ชื่อ                          | เสร็จแล้ว |
| ---------------------------- | -------- |
| Buy groceries                | ไม่       |
| Walk the dog                 | ไม่       |
| Read a chapter of a book     | ใช่       |
| Call mom                     | ใช่       |
| Write the report             | ไม่       |

ควบคุมด้วย property `todolist.seed-sample-data` (เปิดไว้เป็นค่าเริ่มต้น) การเช็คว่าตารางว่างทำให้ restart แล้วไม่เกิดข้อมูลซ้ำ ตั้งค่าเป็น `false` เพื่อเริ่มด้วยลิสต์ว่าง

---

## ข้อมูลและการจัดเก็บ

- ข้อมูลเก็บในฐานข้อมูล **H2 แบบไฟล์** ที่ฝังในแอป ตำแหน่ง `./data/todolist.mv.db` (เทียบกับ working directory)
- โฟลเดอร์ `data/` ถูก **ignore ใน git** และถูกสร้างอัตโนมัติเมื่อรันครั้งแรก
- schema ดูแลโดย Hibernate (`ddl-auto=update`) — ไม่ได้ใช้เครื่องมือ migration
- **ล้างข้อมูลทั้งหมด:** หยุดแอปก่อน แล้วลบโฟลเดอร์ `data/`:

  ```bash
  rm -rf data
  ```

> **หมายเหตุรันทีละอินสแตนซ์:** ฐานข้อมูล H2 แบบไฟล์เปิดได้ทีละ process เท่านั้น ถ้ามีอินสแตนซ์เก่ายังถือไฟล์อยู่ ตัวใหม่จะสตาร์ทไม่ขึ้นด้วยข้อความ `Unable to determine Dialect without JDBC metadata` ให้หยุดอินสแตนซ์เก่าก่อนเริ่มตัวใหม่

---

## โครงสร้างโปรเจกต์

```
.
├── CONTEXT.md                         # อภิธานศัพท์ของโดเมน
├── pom.xml                            # Maven build
├── mvnw / mvnw.cmd                    # Maven wrapper
├── .mvn/wrapper/maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java/com/todolist
│   │   │   ├── TodolistApplication.java     # จุดเริ่มต้นของ Spring Boot
│   │   │   └── task                         # ฟีเจอร์ Task
│   │   │       ├── Task.java                # JPA entity
│   │   │       ├── TaskRepository.java      # Spring Data repository
│   │   │       ├── TaskService.java         # โลจิกทางธุรกิจ
│   │   │       ├── TaskController.java      # REST endpoints
│   │   │       ├── CreateTaskRequest.java   # Input DTO (create)
│   │   │       ├── UpdateTaskRequest.java   # Input DTO (patch)
│   │   │       ├── TaskResponse.java        # Output DTO
│   │   │       ├── TaskNotFoundException.java
│   │   │       ├── ApiExceptionHandler.java # จัดการ error แบบ RFC 7807
│   │   │       └── SampleDataConfiguration.java  # seed ข้อมูลตอนเริ่มระบบ
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java/com/todolist
│       │   ├── OpenApiDocsTest.java
│       │   └── task
│       │       ├── TaskApiTest.java
│       │       └── SampleDataTest.java
│       └── resources
│           └── application.properties       # H2 in-memory สำหรับเทส
└── data/                              # สร้างตอนรัน (ignore ใน git)
```

โค้ดแบ่งชั้นเป็น **Controller → Service → Repository** ไม่ expose JPA entity โดยตรง แต่ใช้ DTO ในการรับส่งข้อมูล

---

## การทดสอบ

รันชุดเทสทั้งหมด:

```bash
./mvnw test
```

- **Integration test 16 รายการ** ทดสอบผ่านชั้น HTTP ด้วย `MockMvc` (create, validation, list/filter/sort, get, patch, delete, not-found, OpenAPI, sample data)
- เทสรันบนฐานข้อมูล **H2 in-memory** (`ddl-auto=create-drop`) แยกจากข้อมูลจริงในเครื่อง
- เทสของ API แต่ละตัวเป็น transactional จึงไม่ทิ้ง state ไปรบกวนกัน

build โดยไม่รันเทส:

```bash
./mvnw clean package -DskipTests
```

---

## ภาษาของโดเมน

โปรเจกต์เก็บอภิธานศัพท์สั้นๆ ไว้ใน [`CONTEXT.md`](CONTEXT.md) คำหลักสองคำ:

- **Task** — งานหนึ่งชิ้นที่ต้องทำ ติดตามอยู่ในลิสต์ (เลี่ยงใช้: *Todo, item, note, entry*)
- **Completed** — สถานะของ Task ที่งานเสร็จแล้ว Task จะเสร็จหรือไม่เสร็จเท่านั้น

---

## แก้ปัญหา

**พอร์ต 8080 ถูกใช้อยู่**

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN     # หา process
kill <PID>                            # หยุดมัน
```

หรือรันบนพอร์ตอื่น:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

**เจอ `Unable to determine Dialect without JDBC metadata` ตอนสตาร์ท**

มีอินสแตนซ์เก่ายังถือ lock ของไฟล์ H2 อยู่ ให้หยุดอินสแตนซ์ที่รันอยู่ก่อน แล้วเริ่มใหม่

**ฐานข้อมูลดูไม่อัปเดต**

ลบโฟลเดอร์ `data/` แล้วเริ่มใหม่ เพื่อสร้าง schema ใหม่และ seed ข้อมูลใหม่ (ถ้าเปิดไว้)

---

## License

โปรเจกต์นี้ยังไม่ได้ระบุ license
