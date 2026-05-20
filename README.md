# Catalog academic - proiect PAOJ 2026

Aplicatie de consola in Java pentru gestionarea unui catalog academic extins:
departamente, studenti, profesori, cursuri, sali, inscrieri, note, prezente si orar.
Datele sunt persistate intr-o baza de date MySQL prin JDBC.

Proiectul acopera **Etapa I** (modelare OOP + colectii + meniu) si **Etapa II**
(persistenta + audit) din cerintele cursului.

## Cuprins

- [Cerinte](#cerinte)
- [Pornire rapida](#pornire-rapida)
- [Structura proiectului](#structura-proiectului)
- [Schema bazei de date](#schema-bazei-de-date)
- [Modelul de date](#modelul-de-date)
- [Meniul aplicatiei](#meniul-aplicatiei)
- [Exemple de utilizare](#exemple-de-utilizare)
- [Audit](#audit)
- [Probleme frecvente](#probleme-frecvente)

## Cerinte

- JDK 25
- Maven 3.8+
- Docker / Docker Desktop (pentru MySQL local)

## Pornire rapida

1. Porneste containerul MySQL:

```
docker compose up -d
```

Serviciul `catalog-mysql` ruleaza pe portul **3307** (user `student`, parola `student`,
baza de date `catalog`).

2. Compileaza si ruleaza aplicatia:

```
mvn clean compile
mvn exec:java
```

La pornire, schema este creata automat din `src/main/resources/schema.sql` daca
tabelele nu exista (CREATE TABLE IF NOT EXISTS).

3. La iesire alege optiunea `0` din meniu. Pentru oprirea bazei de date:

```
docker compose down
```

## Structura proiectului

```
PAOJ-proiect/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/ro/unibuc/catalog/
        │   ├── Main.java                  - meniu interactiv
        │   ├── config/                    - properties, conexiune JDBC, init schema
        │   ├── exception/                 - exceptii custom (RuntimeException)
        │   ├── model/                     - entitati, enums, interfata Printable
        │   ├── repository/                - DAO-uri (PreparedStatement, try-with-resources)
        │   └── service/                   - business logic + AuditService singleton
        └── resources/
            ├── application.properties     - conexiune BD
            └── schema.sql                 - DDL pentru tabele
```

Layere:
- `repository` - acces direct la BD (SQL + JDBC), fara reguli de business
- `service` - validari, reguli de business, audit, compun apeluri pe repos
- `Main` - citeste input din consola si apeleaza serviciile

## Schema bazei de date

```
departments      (id PK, name, code UNIQUE)
students         (id PK, first_name, last_name, email, registration_number UNIQUE,
                  status, department_id FK -> departments)
professors       (id PK, first_name, last_name, email, title,
                  department_id FK -> departments)
classrooms       (id PK, name, capacity, building)
courses          (id PK, name, code UNIQUE, credits, type,
                  department_id FK -> departments,
                  professor_id FK -> professors  NULL)
enrollments      (id PK, student_id FK, course_id FK, academic_year,
                  UNIQUE (student_id, course_id))
grades           (id PK, student_id FK, course_id FK, value, weight, evaluation_type,
                  CHECK value IN [1.0, 10.0], CHECK weight IN (0, 1])
attendances      (id PK, student_id FK, course_id FK, attendance_date, status)
schedule_entries (id PK, course_id FK, classroom_id FK, week_day, start_hour, end_hour)
```

Reguli ON DELETE:
- `enrollments`, `grades`, `attendances` -> CASCADE pe `student_id` si `course_id`
  (cand stergi un student/curs, datele aferente dispar)
- `courses.professor_id` -> SET NULL daca profesorul e sters

## Modelul de date

```
Person (abstract)
  ├── Student      (registrationNumber, status, departmentId)
  └── Professor    (title, departmentId)

Department         (name, code)
Course             (name, code, credits, type, departmentId, professorId?)
Classroom          (name, capacity, building)
Enrollment         (studentId -> courseId, academicYear)
Grade              (studentId, courseId, value, weight, evaluationType)
Attendance        (studentId, courseId, date, status)
ScheduleEntry      (courseId, classroomId, weekDay, startHour, endHour)
```

Enums:
- `StudentStatus`: `ACTIVE`, `GRADUATED`, `SUSPENDED`, `WITHDRAWN`
- `CourseType`: `MANDATORY`, `OPTIONAL`, `ELECTIVE`
- `AttendanceStatus`: `PRESENT`, `ABSENT`, `EXCUSED`
- `WeekDay`: `MONDAY` ... `FRIDAY`

Interfata `Printable` (cu `printDetails()`) e implementata de toate clasele entitate
afisabile (Student, Professor, Department, Course, Classroom, Enrollment, Grade,
Attendance, ScheduleEntry).

Reguli de business notabile (toate validate in `service`):
- email-ul trebuie sa contina `@`
- nota e in `[1.0, 10.0]`, ponderea in `(0, 1]`
- un student nu poate fi inscris de doua ori la acelasi curs (`DuplicateException`)
- prezentele se inregistreaza doar pentru studenti inscrisi la cursul respectiv
- nu poti sterge un student care are inscrieri (`DeleteNotAllowedException`)
- ora de sfarsit a unei intrari in orar trebuie sa fie strict dupa cea de start

## Meniul aplicatiei

```
 1. Adauga departament
 2. Listeaza departamente
 3. Adauga student
 4. Listeaza studenti (sortat dupa nume)
 5. Adauga profesor
 6. Listeaza profesori
 7. Adauga curs
 8. Listeaza cursuri (sortat dupa cod)
 9. Asigneaza profesor la curs
10. Adauga sala
11. Listeaza sali
12. Inscrie student la curs
13. Listeaza inscrieri pentru un curs
14. Adauga nota
15. Listeaza note pentru un student
16. Calculeaza media ponderata student/curs
17. Inregistreaza prezenta
18. Listeaza prezente la curs intr-o zi
19. Adauga intrare orar
20. Orar pentru un curs
21. Orar complet
22. Sterge student
23. Sterge curs
 0. Iesire
```

## Exemple de utilizare

### Scenariu 1 - configurare initiala si inscriere

```
> 1                              # Adauga departament
Nume departament: Matematica si Informatica
Cod departament: MATE
Creat: Department #1 | MATE - Matematica si Informatica

> 5                              # Adauga profesor
Prenume: Ion
Nume: Popescu
Email: ion.popescu@unibuc.ro
Titlu: conf
Id departament: 1
Creat: Professor #1 | conf Ion Popescu | ion.popescu@unibuc.ro

> 7                              # Adauga curs
Nume curs: Programare avansata pe obiecte
Cod curs: PAOJ
Credite: 6
Tip: MANDATORY
Id departament: 1
Creat: Course #1 | PAOJ - Programare avansata pe obiecte | 6 credits | MANDATORY | no professor

> 9                              # Asigneaza profesor la curs
Id curs: 1
Id profesor: 1
OK.

> 3                              # Adauga student
Prenume: Maria
Nume: Ionescu
Email: maria.ionescu@stud.unibuc.ro
Numar matricol: M2025-0042
Id departament: 1
Creat: Student #1 | Maria Ionescu (M2025-0042) | maria.ionescu@stud.unibuc.ro | ACTIVE

> 12                             # Inscrie student la curs
Id student: 1
Id curs: 1
An academic: 2025-2026
Creat: Enrollment #1 | student 1 -> course 1 | 2025-2026
```

### Scenariu 2 - note si medie ponderata

```
> 14                             # Adauga nota partial
Id student: 1
Id curs: 1
Nota: 9.0
Pondere: 0.4
Tip evaluare: midterm
Creat: Grade #1 | student 1 | course 1 | 9.0 (w=0.4) | midterm

> 14                             # Adauga nota examen final
Id student: 1
Id curs: 1
Nota: 8.0
Pondere: 0.6
Tip evaluare: final
Creat: Grade #2 | student 1 | course 1 | 8.0 (w=0.6) | final

> 16                             # Calculeaza media ponderata
Id student: 1
Id curs: 1
Media: 8.40
```

### Scenariu 3 - orar

```
> 10                             # Adauga sala
Nume sala: A2
Capacitate: 80
Cladire: Pitar Mos
Creat: Classroom #1 | Pitar Mos A2 | 80 seats

> 19                             # Adauga intrare orar
Id curs: 1
Id sala: 1
Zi: MONDAY
Start: 10:00
Stop: 12:00
Creat: Schedule #1 | course 1 in classroom 1 | MONDAY 10:00-12:00

> 20                             # Orar pentru un curs
Id curs: 1
Schedule #1 | course 1 in classroom 1 | MONDAY 10:00-12:00
```

### Scenariu 4 - validari care esueaza intentionat

```
> 12                             # Inscriere duplicata
Id student: 1
Id curs: 1
An academic: 2025-2026
[!] Student is already enrolled in this course

> 22                             # Stergere student inscris
Id student: 1
[!] Student 1 is enrolled in courses and cannot be deleted

> 14                             # Nota in afara intervalului
Id student: 1
Id curs: 1
Nota: 12
Pondere: 1
Tip evaluare: bonus
[!] Grade must be between 1.0 and 10.0
```

## Audit

Toate actiunile relevante sunt scrise in `audit.csv` (in radacina proiectului) cu
timestamp ISO-8601:

```
ADD_DEPARTMENT,2026-04-02T14:23:11
ADD_PROFESSOR,2026-04-02T14:23:47
ADD_COURSE,2026-04-02T14:24:05
ENROLL_STUDENT,2026-04-02T14:25:12
ADD_GRADE,2026-04-02T14:30:08
COMPUTE_AVERAGE,2026-04-02T14:31:22
```

Fisierul `audit.csv` este in `.gitignore` (este artefact local).

## Probleme frecvente

**Eroare `Communications link failure` la pornire**
Containerul MySQL nu e pornit sau inca initializeaza. Verifica:
```
docker compose ps
docker compose logs catalog-mysql
```

**Portul 3307 e ocupat**
Schimba mapping-ul in `docker-compose.yml` (`"3308:3306"`) si actualizeaza
`db.url` in `src/main/resources/application.properties`.

**`mvn` nu este recunoscut**
Adauga Maven in PATH sau ruleaza via wrapper-ul IDE (IntelliJ are Run Configuration
pe `Main.java`).

**Vrei sa resetezi baza de date**
```
docker compose down -v
docker compose up -d
```
Volumul `catalog_data` este sters si schema se recreeaza la prima rulare.
