# Hospital Management System

A REST API built with **Java, Spring Boot, Spring Data JPA and MySQL** that manages patients, doctors, departments,
appointments, prescriptions and bills.

The focus is on **real backend business logic**, not just CRUD: preventing double bookings under concurrent requests,
preventing double payments, enforcing a clear workflow between modules, and returning consistent error responses.

---

## Table of Contents

1. [What the system does](#what-the-system-does)
2. [Tech stack](#tech-stack)
3. [Architecture](#architecture)
4. [Data model](#data-model)
5. [Business rules](#business-rules)
6. [Engineering highlights](#engineering-highlights)
7. [API reference](#api-reference)
8. [Error responses](#error-responses)
9. [API documentation (Swagger)](#api-documentation-swagger)
10. [Getting started](#getting-started)
11. [Quick start walkthrough](#quick-start-walkthrough)
12. [Testing](#testing)
13. [Project structure](#project-structure)
14. [Future improvements](#future-improvements)
15. [Author](#author)

---

## What the system does

A typical hospital visit flows through the modules in this order:

```
Department ──► Doctor ──► Appointment ──► Prescription
                 ▲            │
Patient ─────────┘            └──────────► Bill (payment)
```

| Module            | What it does                                                                                                          |
|-------------------|-----------------------------------------------------------------------------------------------------------------------|
| **Patients**      | Register, view, update, delete, search (name, age, blood group) and paginate patients.                                |
| **Departments**   | Hospital departments (Cardiology, Neurology, ...). A department cannot be deleted while doctors belong to it.         |
| **Doctors**       | Doctors belong to a department and have a consultation fee. Email and license number must be unique.                  |
| **Appointments**  | Book a patient with a doctor for a time slot. Cancel, complete or reschedule. Overlapping bookings are rejected.      |
| **Prescriptions** | A doctor records a diagnosis and a list of medicines for a **completed** appointment.                                 |
| **Bills**         | A bill is generated for a **completed** appointment (consultation fee + medicine charges) and then paid or cancelled. |

---

## Tech stack

- Java and Spring Boot (Web, Validation)
- Spring Data JPA and Hibernate
- MySQL 8
- Lombok
- Springdoc OpenAPI / Swagger UI
- Maven
- JUnit 5, Mockito and AssertJ
- Git and GitHub

---

## Architecture

Layered architecture with DTOs between the API and the database:

```
Client (Swagger UI / Postman)
        │
        ▼
   Controller          HTTP, request validation (@Valid), status codes
        │
        ▼
 Service (interface)   business contract
        │
        ▼
 ServiceImpl           business rules, transactions, mapping to DTOs
        │
        ▼
   Repository          Spring Data JPA, JPQL queries
        │
        ▼
     MySQL
```

- **DTOs** separate what the API accepts and returns from what is stored. Clients never send an entity, and never choose
  server-controlled fields such as a status.
- **Constructor injection** is used in services and controllers (immutable dependencies, easy to unit test).
- **Centralised error handling** with `@RestControllerAdvice` maps exceptions to the correct HTTP status.

---

## Data model

```
Department 1 ────── * Doctor 1 ────── * Appointment * ────── 1 Patient
                                            │
                                            ├── 0..1 Prescription 1 ── * PrescriptionItem
                                            └── 0..1 Bill
```

| Entity             | Key fields                                                                            | Relationships                                                                          |
|--------------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `Department`       | name (unique), description, location, phone                                           | one-to-many Doctor                                                                     |
| `Doctor`           | name, email (unique), licenseNumber (unique), experience, salary, consultationFee     | many-to-one Department                                                                 |
| `Patient`          | name, age, gender, email, phone, bloodGroup                                           |                                                                                        |
| `Appointment`      | startTime, endTime, status, reason                                                    | many-to-one Patient, many-to-one Doctor                                                |
| `Prescription`     | diagnosis, notes, createdAt                                                           | one-to-one Appointment (unique), one-to-many PrescriptionItem (cascade, orphanRemoval) |
| `PrescriptionItem` | medicineName, dosage, frequency, durationDays, instructions                           | many-to-one Prescription                                                               |
| `Bill`             | consultationFee, medicineCharges, totalAmount, status, paymentMethod, paidAt, version | one-to-one Appointment (unique)                                                        |

**Enums** (stored as text, not ordinals):

- `AppointmentStatus`: `SCHEDULED`, `COMPLETED`, `CANCELLED`
- `BillStatus`: `PENDING`, `PAID`, `CANCELLED`
- `PaymentMethod`: `CASH`, `CARD`, `UPI`

---

## Business rules

**Doctors and departments**

- Doctor email and license number must be unique (checked in code, backed by database unique constraints).
- On update, a doctor may keep their own email and license, but cannot take another doctor's.
- Bulk doctor creation rejects duplicates **inside the same request** and against existing data, and is all-or-nothing.
- Department names are unique, ignoring case ("Cardiology" and "cardiology" clash).
- A department that still has doctors cannot be deleted.

**Appointments**

- End time must be after start time, and the start time must be in the future.
- A doctor cannot have two `SCHEDULED` appointments that overlap in time.
- A patient cannot have two `SCHEDULED` appointments that overlap in time.
- Back-to-back slots are allowed (10:00-10:30 then 10:30-11:00).
- Cancelled slots become free again.
- Only a `SCHEDULED` appointment can be cancelled, completed or rescheduled.
- Rescheduling ignores the appointment's own current slot when checking for overlap.

**Prescriptions**

- Allowed only for `COMPLETED` appointments.
- At most one prescription per appointment.
- Updating a prescription replaces its medicine list.

**Bills**

- Allowed only for `COMPLETED` appointments, one bill per appointment.
- `totalAmount = doctor's consultation fee + medicine charges`, calculated with `BigDecimal`.
- The consultation fee is **copied into the bill** (snapshot), so changing the doctor's fee later does not change old
  bills.
- Only a `PENDING` bill can be paid or cancelled. A paid bill cannot be paid again or cancelled.

---

## Engineering highlights

| Topic                                | How it is handled                                                                                                                                                                                                         |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Double booking under concurrency** | The booking transaction takes a **pessimistic write lock** (`SELECT ... FOR UPDATE`) on the doctor row, so simultaneous bookings for the same doctor run one at a time and the overlap check always sees the latest data. |
| **Overlap detection**                | JPQL range query: `existing.start < new.end AND existing.end > new.start`.                                                                                                                                                |
| **Double payment**                   | **Optimistic locking** with `@Version` on `Bill`. A conflicting concurrent update fails and is returned as `409`.                                                                                                         |
| **Duplicate data races**             | Friendly `existsBy...` checks for clear messages, plus database `UNIQUE` constraints as the final safety net. `DataIntegrityViolationException` is mapped to `409`.                                                       |
| **Transactions**                     | Services are `@Transactional(readOnly = true)` by default, with write methods overriding it. Multi-step operations are all-or-nothing.                                                                                    |
| **N+1 queries**                      | `@EntityGraph` fetches related department, doctor and patient data in one query for list and detail endpoints.                                                                                                            |
| **Money**                            | `BigDecimal` with `precision = 10, scale = 2`. Never `double`.                                                                                                                                                            |
| **Parent-child data**                | `Prescription` owns its items with `cascade = ALL` and `orphanRemoval = true`. A helper keeps both sides of the relationship in sync.                                                                                     |
| **Validation**                       | Bean Validation on DTOs (`@NotBlank`, `@Email`, `@Future`, `@PositiveOrZero`, nested `@Valid`).                                                                                                                           |
| **Pagination and sorting**           | `Pageable` on list endpoints: `?page=0&size=10&sort=name,asc`.                                                                                                                                                            |

---

## API reference

Base URL: `http://localhost:8080`

### Patients `/patients`

| Method | Path                                                          | Description                           |
|--------|---------------------------------------------------------------|---------------------------------------|
| POST   | `/patients`                                                   | Create a patient                      |
| POST   | `/patients/bulk`                                              | Create many patients                  |
| GET    | `/patients/{id}`                                              | Get one                               |
| GET    | `/patients`                                                   | Paginated list                        |
| GET    | `/patients/search/name?name=`                                 | Search by name                        |
| GET    | `/patients/search/age?age=`                                   | Patients older than an age            |
| GET    | `/patients/search/patients?name=&bloodGroup=&minAge=&maxAge=` | Combined optional filters (paginated) |
| PUT    | `/patients/{id}`                                              | Update                                |
| DELETE | `/patients/{id}`                                              | Delete                                |

### Departments `/departments`

| Method | Path                 | Description                          |
|--------|----------------------|--------------------------------------|
| POST   | `/departments`       | Create (409 on duplicate name)       |
| POST   | `/departments/bulk`  | Create many (all or nothing)         |
| GET    | `/departments/{id}`  | Get one                              |
| GET    | `/departments?name=` | Paginated list, optional name filter |
| PUT    | `/departments/{id}`  | Update                               |
| DELETE | `/departments/{id}`  | Delete (409 if doctors are assigned) |

### Doctors `/doctors`

| Method | Path                                                                      | Description                           |
|--------|---------------------------------------------------------------------------|---------------------------------------|
| POST   | `/doctors`                                                                | Create                                |
| POST   | `/doctors/bulk`                                                           | Create many (all or nothing)          |
| GET    | `/doctors/{id}`                                                           | Get one                               |
| GET    | `/doctors`                                                                | Paginated list                        |
| GET    | `/doctors/search/doctors?name=&department=&minExperience=&maxExperience=` | Combined optional filters (paginated) |
| PUT    | `/doctors/{id}`                                                           | Update (full replace)                 |
| DELETE | `/doctors/{id}`                                                           | Delete                                |

### Appointments `/appointments`

| Method | Path                                | Description                                    |
|--------|-------------------------------------|------------------------------------------------|
| POST   | `/appointments`                     | Book (409 on doctor or patient overlap)        |
| GET    | `/appointments/{id}`                | Get one                                        |
| GET    | `/appointments`                     | Paginated list                                 |
| GET    | `/appointments/doctor/{doctorId}`   | A doctor's appointments (paginated)            |
| GET    | `/appointments/patient/{patientId}` | A patient's appointments (paginated)           |
| PATCH  | `/appointments/{id}/cancel`         | Cancel                                         |
| PATCH  | `/appointments/{id}/complete`       | Mark completed                                 |
| PUT    | `/appointments/{id}/reschedule`     | Change the time (body: `startTime`, `endTime`) |

### Prescriptions `/prescriptions`

| Method | Path                                         | Description                            |
|--------|----------------------------------------------|----------------------------------------|
| POST   | `/prescriptions`                             | Create for a completed appointment     |
| GET    | `/prescriptions/{id}`                        | Get one                                |
| GET    | `/prescriptions/appointment/{appointmentId}` | Get the prescription of an appointment |
| PUT    | `/prescriptions/{id}`                        | Replace diagnosis, notes and medicines |
| DELETE | `/prescriptions/{id}`                        | Delete (items are deleted with it)     |

### Bills `/bills`

| Method | Path                                 | Description                                 |
|--------|--------------------------------------|---------------------------------------------|
| POST   | `/bills`                             | Generate a bill for a completed appointment |
| GET    | `/bills/{id}`                        | Get one                                     |
| GET    | `/bills/appointment/{appointmentId}` | Get the bill of an appointment              |
| GET    | `/bills?status=PENDING`              | Paginated list, optional status filter      |
| PATCH  | `/bills/{id}/pay`                    | Pay (body: `paymentMethod`)                 |
| PATCH  | `/bills/{id}/cancel`                 | Cancel a pending bill                       |

---

## Error responses

All business errors use the same shape:

```json
{
  "error": "Doctor is not available in this time slot"
}
```

Validation errors list the field and message:

```json
{
  "name": "Name is required",
  "email": "Invalid email format"
}
```

| Status            | When                                                                                                                                                                                         |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `400 Bad Request` | Validation failure, end time before start time, malformed JSON, invalid enum value                                                                                                           |
| `404 Not Found`   | Patient, doctor, department, appointment, prescription or bill does not exist                                                                                                                |
| `409 Conflict`    | Duplicate email, license or department name; overlapping appointment; wrong status for the action (for example paying a paid bill); department still has doctors; concurrent update conflict |

---

## API documentation (Swagger)

Interactive API docs are generated automatically with Springdoc OpenAPI. Start the app, then open:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

You can try every endpoint from the browser: pick an endpoint, click **Try it out**, edit the request body and press *
*Execute**.

---

## Getting started

### Prerequisites

- JDK 17 or newer
- Maven
- MySQL 8

### 1. Clone

```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

### 2. Create the database

```sql
CREATE
DATABASE hospital_db;
```

### 3. Configure `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

Set `DB_USERNAME` and `DB_PASSWORD` as environment variables. **Never commit real credentials.**

Tables are created automatically by Hibernate on first run.

### 4. Run

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

---

## Quick start walkthrough

Follow this order to see the whole workflow. Use Postman or curl.

**1. Create a department**

`POST /departments`

```json
{
  "name": "Cardiology",
  "description": "Heart care",
  "location": "Block A, Floor 2",
  "phone": "044-2345001"
}
```

**2. Create a doctor** (use the department id returned above)

`POST /doctors`

```json
{
  "name": "Arjun Venkatesh",
  "age": 38,
  "gender": "M",
  "departmentId": 1,
  "email": "arjun@example.com",
  "phone": "9841234501",
  "address": "Chennai",
  "experience": 13,
  "licenseNumber": "MED-DOC-1101",
  "salary": 450000.00,
  "consultationFee": 800.00
}
```

**3. Create a patient**

`POST /patients`

```json
{
  "name": "Anu",
  "age": 30,
  "gender": "F",
  "email": "anu@example.com",
  "address": "Chennai",
  "phone": "9876543210",
  "bloodGroup": "O+"
}
```

**4. Book an appointment** (use a future date)

`POST /appointments`

```json
{
  "patientId": 1,
  "doctorId": 1,
  "startTime": "2027-01-20T10:00:00",
  "endTime": "2027-01-20T10:30:00",
  "reason": "Chest pain"
}
```

Try booking `10:15` to `10:45` for the same doctor. You get `409`.

**5. Complete the appointment**

`PATCH /appointments/1/complete`

**6. Write a prescription**

`POST /prescriptions`

```json
{
  "appointmentId": 1,
  "diagnosis": "Migraine",
  "notes": "Avoid bright light",
  "items": [
    {
      "medicineName": "Paracetamol",
      "dosage": "500 mg",
      "frequency": "Twice a day",
      "durationDays": 5,
      "instructions": "After food"
    }
  ]
}
```

**7. Generate and pay the bill**

`POST /bills`

```json
{
  "appointmentId": 1,
  "medicineCharges": 250.50
}
```

`PATCH /bills/1/pay`

```json
{
  "paymentMethod": "UPI"
}
```

The bill total is `800.00 + 250.50 = 1050.50`. Paying it again returns `409`.

---

## Testing

```bash
mvn test
```

Unit tests cover every service class (`Appointment`, `Bill`, `Doctor`, `Department`, `Patient`, `Prescription`) using
JUnit 5, Mockito and AssertJ:

- happy paths and field mapping
- not-found cases
- business-rule violations (overlaps, wrong status, duplicates, missing fee)
- verification that nothing is saved when a rule fails

Not covered yet: repository query tests, controller tests, and concurrency tests (see below).

---

## Project structure

```
src/
├── main/
│   ├── java/com/example/HospitalManagementSystem/
│   │   ├── Config/          OpenAPI (Swagger) configuration
│   │   ├── Controller/      REST endpoints
│   │   ├── DTO/             request and response models
│   │   ├── Entity/          JPA entities
│   │   ├── Enum/            AppointmentStatus, BillStatus, PaymentMethod
│   │   ├── Exception/       custom exceptions and GlobalExceptionHandler
│   │   ├── Repository/      Spring Data JPA repositories
│   │   ├── Service/         service interfaces
│   │   ├── ServiceImpl/     business logic
│   │   └── HospitalManagementSystemApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/HospitalManagementSystem/ServiceImpl/
```

---

## Future improvements

- Spring Security with JWT and roles (admin, doctor, patient), including "a patient can only see their own records"
- `@DataJpaTest` tests for the overlap query
- `@WebMvcTest` tests for controllers and validation
- Concurrency integration test for double booking
- Flyway for database migrations instead of `ddl-auto=update`
- Docker and Docker Compose (app + MySQL)
- GitHub Actions CI to run tests on every push
- Per-medicine pricing so bill medicine charges are calculated automatically

---

## Author

Renuga R

Java Developer | Spring Boot | Microservices | REST APIs | SQL

[GitHub](https://github.com/your-username) · [LinkedIn](www.linkedin.com/in/renugachitra30)
