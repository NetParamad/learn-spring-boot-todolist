[EN](README.md) | [TH](README.th.md)

<h1 align="center">Todolist</h1>

<p align="center">
  ระบบรายการงานแบบผู้ใช้คนเดียวที่เปิดผ่าน JSON REST API — สร้าง Task, ทำเครื่องหมายว่าเสร็จ และลบ Task
</p>

<p align="center">
  <a href="#ภาพรวม">ภาพรวม</a> ·
  <a href="#ฟีเจอร์">ฟีเจอร์</a> ·
  <a href="#เทคโนโลยีที่ใช้">เทคโนโลยี</a> ·
  <a href="#โครงสร้างโปรเจกต์">โครงสร้าง</a> ·
  <a href="#การตั้งค่า">การตั้งค่า</a> ·
  <a href="#เริ่มต้นใช้งาน">เริ่มต้นใช้งาน</a> ·
  <a href="#คำสั่งสคริปต์">สคริปต์</a> ·
  <a href="#วิธีใช้งาน">วิธีใช้งาน</a> ·
  <a href="#หมายเหตุ">หมายเหตุ</a>
</p>

---

## ภาพรวม

Todolist เป็นโปรเจกต์เพื่อการเรียนรู้ที่สร้างบน **Spring Boot 4.1.1** และ **Java 25** เปิดระบบรายการงานแบบผู้ใช้คนเดียวผ่าน JSON REST API ใช้ฐานข้อมูล **H2** แบบไฟล์ที่ฝังในแอป พร้อมเอกสารแบบอินเทอร์แอกทีฟผ่าน **Swagger UI**

โค้ดแบ่งชั้นเป็น Controller → Service → Repository และไม่ expose JPA entity โดยตรง คำขอและคำตอบใช้ DTO ส่วน Task สร้างได้ เปลี่ยนชื่อได้ ทำเครื่องหมายเสร็จ/ยกเลิกเสร็จได้ กรองตามสถานะได้ และลบได้ โปรเจกต์นี้ไม่มีระบบยืนยันตัวตนและไม่รองรับผู้ใช้หลายคน

## ฟีเจอร์

- **Task CRUD** — สร้าง อ่าน แก้ไข และลบ Task ผ่าน REST API
- **ทำเครื่องหมายเสร็จ / ยกเลิกเสร็จ** — สลับค่า `completed` โดยไม่มีสถานะกึ่งกลาง
- **กรองตามสถานะ** — `all` (ค่าเริ่มต้น), `active` หรือ `completed`
- **เรียงใหม่สุดก่อน** — เรียงตาม `createdAt` จากมากไปน้อย แล้วตามด้วย `id` จากมากไปน้อย
- **ตรวจสอบข้อมูลนำเข้า** — `POST /tasks` ต้องมี `title` ที่ไม่เว้นว่าง ไม่อย่างนั้นจะได้ `400 Bad Request`
- **Error แบบ RFC 7807** — ทุก error ใช้ `application/problem+json`
- **OpenAPI docs** — สร้างอัตโนมัติด้วย springdoc-openapi และแสดงใน Swagger UI
- **จัดเก็บถาวร** — ฐานข้อมูล H2 แบบไฟล์ที่ `./data/todolist.mv.db`
- **ข้อมูลตัวอย่างแบบเลือกได้** — seed Task 5 รายการตอนเริ่มระบบถ้าตารางว่าง
- **Integration test** — 16 เทสทดสอบชั้น HTTP ด้วย MockMvc

## เทคโนโลยีที่ใช้

| ส่วนประกอบ | เวอร์ชัน / ที่เลือกใช้ |
| --- | --- |
| ภาษา | Java 25 |
| เฟรมเวิร์ก | Spring Boot 4.1.1 |
| เว็บ | Spring Web MVC (`spring-boot-starter-webmvc`) |
| ชั้นจัดเก็บข้อมูล | Spring Data JPA + Hibernate ORM |
| ฐานข้อมูล | H2 (แบบไฟล์ ฝังในแอป) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| เอกสาร API | springdoc-openapi 3.1.1 + Swagger UI |
| Build | Maven พร้อม wrapper ที่แนบมา |
| การทดสอบ | JUnit Jupiter + MockMvc (`spring-boot-starter-webmvc-test`) |

## โครงสร้างโปรเจกต์

```
.
├── CONTEXT.md                     # Domain glossary
├── README.md / README.th.md
├── pom.xml                        # Maven build
├── mvnw / mvnw.cmd                # Maven wrapper
├── .mvn/wrapper/maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java/com/todolist
│   │   │   ├── TodolistApplication.java
│   │   │   └── task/              # Task feature
│   │   └── resources/application.properties
│   └── test
│       ├── java/com/todolist/     # OpenApiDocsTest, task/TaskApiTest, task/SampleDataTest
│       └── resources/application.properties   # In-memory H2 for tests
└── data/                          # Generated at runtime (git-ignored)
```

แพ็กเกจ `task` ประกอบด้วย `Task` (JPA entity), `TaskRepository`, `TaskService`, `TaskController`, DTO สำหรับ create/update/response, `TaskNotFoundException`, `ApiExceptionHandler` และ `SampleDataConfiguration` การแบ่งชั้น Controller → Service → Repository ช่วยแยกเรื่อง persistence ออกจากชั้น HTTP

## การตั้งค่า

ค่าต่างๆ อยู่ใน `src/main/resources/application.properties`:

| Property | ค่าเริ่มต้น | หน้าที่ |
| --- | --- | --- |
| `spring.application.name` | `todolist` | ชื่อแอปพลิเคชัน |
| `spring.datasource.url` | `jdbc:h2:file:./data/todolist;DB_CLOSE_ON_EXIT=FALSE` | ตำแหน่งฐานข้อมูล H2 แบบไฟล์ |
| `spring.datasource.username` | `sa` | ผู้ใช้ฐานข้อมูล |
| `spring.datasource.password` | *(ว่าง)* | รหัสผ่านฐานข้อมูล |
| `spring.jpa.hibernate.ddl-auto` | `update` | สร้าง/อัปเดต schema จาก entity |
| `todolist.seed-sample-data` | `true` | seed Task 5 รายการตอนเริ่มระบบถ้าตารางว่าง |

เทสใช้ `src/test/resources/application.properties` ซึ่งเปลี่ยนเป็นฐานข้อมูล H2 **in-memory** และ `ddl-auto=create-drop` เทสจึงไม่แตะฐานข้อมูลไฟล์จริง

เมื่อเปิด seed ไว้ แอปจะเพิ่ม Task 5 รายการเมื่อตาราง `tasks` ว่าง: Buy groceries, Walk the dog, Read a chapter of a book, Call mom และ Write the report การ restart จะไม่ทำให้ข้อมูลซ้ำ

ถ้าต้องการเริ่มโดยไม่มีข้อมูลตัวอย่าง:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--todolist.seed-sample-data=false
```

## เริ่มต้นใช้งาน

ความต้องการ: **JDK 25+** ไม่ต้องติดตั้ง Maven แยกเพราะมี `./mvnw` แนบมาให้ และไม่ต้องมีเซิร์ฟเวอร์ฐานข้อมูลเพราะ H2 ฝังอยู่ในแอป

```bash
java -version     # should report 25.x
git clone https://github.com/NetParamad/learn-spring-boot-todolist.git
cd learn-spring-boot-todolist
./mvnw spring-boot:run
```

API ให้บริการที่ **http://localhost:8080** ทดสอบเร็วๆ: `curl http://localhost:8080/tasks`

สร้างไฟล์ jar ที่รันได้:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### คำสั่งสคริปต์

| คำสั่ง | คำอธิบาย |
| --- | --- |
| `./mvnw spring-boot:run` | รันแอปที่พอร์ต 8080 |
| `./mvnw test` | รัน integration test 16 รายการบน H2 in-memory |
| `./mvnw clean package` | สร้าง jar และรันเทส |
| `./mvnw clean package -DskipTests` | สร้าง jar โดยไม่รันเทส |
| `./mvnw spring-boot:run -Dspring-boot.run.arguments=--todolist.seed-sample-data=false` | รันโดยไม่มีข้อมูลตัวอย่าง |

## วิธีใช้งาน

Base URL: `http://localhost:8080`

### Endpoints

| Method | Path | สำเร็จ | ข้อผิดพลาด |
| --- | --- | --- | --- |
| `POST` | `/tasks` | `201 Created` | `400` |
| `GET` | `/tasks?status=all\|active\|completed` | `200 OK` | — |
| `GET` | `/tasks/{id}` | `200 OK` | `404` |
| `PATCH` | `/tasks/{id}` | `200 OK` | `400`, `404` |
| `DELETE` | `/tasks/{id}` | `204 No Content` | `404` |

Task ถูกส่งกลับเป็น JSON:

```json
{ "id": 1, "title": "Buy milk", "completed": false, "createdAt": "2026-10-17T16:40:34.714212Z" }
```

1. **สร้าง Task** — `title` จำเป็นและห้ามเว้นว่าง คำขอที่สำเร็จจะได้ `201 Created` พร้อม header `Location`

   ```bash
   curl -i -X POST http://localhost:8080/tasks \
     -H 'Content-Type: application/json' \
     -d '{"title": "Buy milk"}'
   ```

2. **ลิสต์และกรอง** — ค่าเริ่มต้น `status=all` คืนทุก Task เรียงใหม่สุดก่อน ใช้ `active` หรือ `completed` เพื่อกรอง

   ```bash
   curl http://localhost:8080/tasks
   curl "http://localhost:8080/tasks?status=active"
   ```

3. **อ่าน Task เดียว** — `GET /tasks/{id}` คืน Task หรือ `404 Not Found` (`curl http://localhost:8080/tasks/1`)

4. **แก้ไขหรือทำเครื่องหมายเสร็จ** — `PATCH` รับ `title`, `completed` หรือทั้งสองอย่าง ฟิลด์ที่ไม่ส่งจะคงเดิม

   ```bash
   curl -X PATCH http://localhost:8080/tasks/1 \
     -H 'Content-Type: application/json' \
     -d '{"completed": true}'
   ```

5. **ลบ Task** — คืน `204 No Content` หรือ `404` เมื่อไม่มี Task นั้น

   ```bash
   curl -i -X DELETE http://localhost:8080/tasks/1
   ```

6. **สำรวจ API** — เปิด Swagger UI ที่ `http://localhost:8080/swagger-ui/index.html` และ OpenAPI JSON ที่ `http://localhost:8080/v3/api-docs`

ทุก error ใช้ **RFC 7807 Problem Details**:

```json
{ "detail": "Task 999 not found", "instance": "/tasks/999", "status": 404, "title": "Not Found" }
```

Validation ไม่ผ่านจะได้ `400` พร้อม `"detail": "Validation failed"`

## หมายเหตุ

- **ภาษาของโดเมน** — [`CONTEXT.md`](CONTEXT.md) นิยาม **Task** (เลี่ยงใช้ *Todo, item, note, entry*) และ **Completed** (ไม่มีสถานะกึ่งกลาง)
- **การจัดเก็บ** — schema ดูแลโดย Hibernate (`ddl-auto=update`) ไม่มีเครื่องมือ migration หยุดแอปแล้วลบ `data/` เพื่อล้างข้อมูลทั้งหมด
- **รันทีละอินสแตนซ์** — ฐานข้อมูล H2 แบบไฟล์เปิดได้ทีละ process เท่านั้น ถ้าอินสแตนซ์เก่ายังถือ lock อยู่ ให้หยุดก่อนเริ่มตัวใหม่
- **พอร์ตชนกัน** — ถ้าพอร์ต 8080 ถูกใช้อยู่ ให้ต่อท้าย `spring-boot.run.arguments` ด้วย `--server.port=9090`
- **ตัวแปรสภาพแวดล้อม** — ไม่ต้องใช้ environment variable ค่าต่างๆ มาจาก `application.properties` หรือ override ผ่าน command line
- **สัญญาอนุญาต** — MIT (ดู [LICENSE](LICENSE))
