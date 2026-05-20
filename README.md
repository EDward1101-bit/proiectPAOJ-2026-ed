# Catalog academic - proiect PAOJ 2026

Aplicatie de consola in Java pentru gestionarea unui catalog academic: departamente,
studenti, profesori, cursuri, sali, inscrieri, note, prezente si orar. Datele sunt
persistate intr-o baza de date MySQL prin JDBC.

## Cerinte

- JDK 25
- Maven 3.8+
- Docker (pentru MySQL)

## Pornire

1. Porneste baza de date:

```
docker compose up -d
```

Containerul `catalog-mysql` expune MySQL pe portul **3307** (user/parola: `student`/`student`,
baza de date `catalog`).

2. Compileaza si ruleaza aplicatia:

```
mvn clean compile
mvn exec:java
```

La pornire, schema este creata automat din `src/main/resources/schema.sql`.

## Structura proiectului

```
ro.unibuc.catalog
 ├── config        - incarcare properties, conexiune JDBC, init schema
 ├── exception     - exceptii custom (Validation, NotFound, Duplicate, ...)
 ├── model         - entitati, enums, interfata Printable
 ├── repository    - DAO-uri pentru fiecare entitate (PreparedStatement)
 └── service       - business logic + audit, apelate din Main
```

## Schema bazei de date

```
departments  (id, name, code UNIQUE)
students     (id, first_name, last_name, email, registration_number UNIQUE, status, department_id)
professors   (id, first_name, last_name, email, title, department_id)
classrooms   (id, name, capacity, building)
courses      (id, name, code UNIQUE, credits, type, department_id, professor_id NULL)
enrollments  (id, student_id, course_id, academic_year)     UNIQUE(student_id, course_id)
grades       (id, student_id, course_id, value 1.0-10.0, weight, evaluation_type)
attendances  (id, student_id, course_id, attendance_date, status)
schedule_entries (id, course_id, classroom_id, week_day, start_hour, end_hour)
```

Relatii principale:
- `Person` (abstract) -> `Student`, `Professor`
- `Course` apartine unui `Department` si optional unui `Professor`
- `Enrollment`, `Grade`, `Attendance` leaga `Student` si `Course`
- `ScheduleEntry` leaga `Course` si `Classroom`

## Exemple de comenzi din meniu

- `1` Adauga departament -> `Matematica`, `MATE`
- `3` Adauga student -> prenume, nume, email, numar matricol, id departament
- `7` Adauga curs -> nume, cod, credite, tip (MANDATORY/OPTIONAL/ELECTIVE), id departament
- `12` Inscrie student la curs -> id student, id curs, an academic (`2025-2026`)
- `14` Adauga nota -> id student, id curs, valoare (1.0-10.0), pondere (0-1], tip evaluare
- `16` Calculeaza media ponderata -> id student, id curs

## Audit

Toate actiunile relevante sunt scrise in `audit.csv` (in radacina proiectului) sub forma:

```
ADD_STUDENT,2026-04-02T14:23:11
ADD_COURSE,2026-04-02T14:25:47
```
