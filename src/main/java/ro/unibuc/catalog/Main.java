package ro.unibuc.catalog;

import ro.unibuc.catalog.config.DatabaseInitializer;
import ro.unibuc.catalog.model.AttendanceStatus;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.model.Printable;
import ro.unibuc.catalog.model.WeekDay;
import ro.unibuc.catalog.repository.AttendanceRepository;
import ro.unibuc.catalog.repository.ClassroomRepository;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.GradeRepository;
import ro.unibuc.catalog.repository.ProfessorRepository;
import ro.unibuc.catalog.repository.ScheduleRepository;
import ro.unibuc.catalog.repository.StudentRepository;
import ro.unibuc.catalog.service.AttendanceService;
import ro.unibuc.catalog.service.AuditService;
import ro.unibuc.catalog.service.ClassroomService;
import ro.unibuc.catalog.service.CourseService;
import ro.unibuc.catalog.service.DepartmentService;
import ro.unibuc.catalog.service.EnrollmentService;
import ro.unibuc.catalog.service.GradeService;
import ro.unibuc.catalog.service.ProfessorService;
import ro.unibuc.catalog.service.ScheduleService;
import ro.unibuc.catalog.service.StudentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {

    private final Scanner in = new Scanner(System.in);

    private final DepartmentService departmentService;
    private final StudentService studentService;
    private final ProfessorService professorService;
    private final CourseService courseService;
    private final ClassroomService classroomService;
    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final ScheduleService scheduleService;

    public Main() {
        var departmentRepo = new DepartmentRepository();
        var studentRepo = new StudentRepository();
        var professorRepo = new ProfessorRepository();
        var courseRepo = new CourseRepository();
        var classroomRepo = new ClassroomRepository();
        var enrollmentRepo = new EnrollmentRepository();
        var gradeRepo = new GradeRepository();
        var attendanceRepo = new AttendanceRepository();
        var scheduleRepo = new ScheduleRepository();

        AuditService audit = AuditService.getInstance();

        this.departmentService = new DepartmentService(departmentRepo, audit);
        this.studentService = new StudentService(studentRepo, departmentRepo, enrollmentRepo, audit);
        this.professorService = new ProfessorService(professorRepo, departmentRepo, audit);
        this.courseService = new CourseService(courseRepo, departmentRepo, professorRepo, audit);
        this.classroomService = new ClassroomService(classroomRepo, audit);
        this.enrollmentService = new EnrollmentService(enrollmentRepo, studentRepo, courseRepo, audit);
        this.gradeService = new GradeService(gradeRepo, studentRepo, courseRepo, enrollmentRepo, audit);
        this.attendanceService = new AttendanceService(attendanceRepo, studentRepo, courseRepo, enrollmentRepo, audit);
        this.scheduleService = new ScheduleService(scheduleRepo, courseRepo, classroomRepo, audit);
    }

    public static void main(String[] args) {
        DatabaseInitializer.run();
        new Main().run();
    }

    private void run() {
        while (true) {
            printMenu();
            int choice = readInt("Alege: ");
            if (choice == 0) {
                System.out.println("La revedere!");
                return;
            }
            try {
                handle(choice);
            } catch (RuntimeException e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== Catalog academic =====");
        System.out.println(" 1. Adauga departament");
        System.out.println(" 2. Listeaza departamente");
        System.out.println(" 3. Adauga student");
        System.out.println(" 4. Listeaza studenti (sortat dupa nume)");
        System.out.println(" 5. Adauga profesor");
        System.out.println(" 6. Listeaza profesori");
        System.out.println(" 7. Adauga curs");
        System.out.println(" 8. Listeaza cursuri (sortat dupa cod)");
        System.out.println(" 9. Asigneaza profesor la curs");
        System.out.println("10. Adauga sala");
        System.out.println("11. Listeaza sali");
        System.out.println("12. Inscrie student la curs");
        System.out.println("13. Listeaza inscrieri pentru un curs");
        System.out.println("14. Adauga nota");
        System.out.println("15. Listeaza note pentru un student");
        System.out.println("16. Calculeaza media ponderata student/curs");
        System.out.println("17. Inregistreaza prezenta");
        System.out.println("18. Listeaza prezente la curs intr-o zi");
        System.out.println("19. Adauga intrare orar");
        System.out.println("20. Orar pentru un curs");
        System.out.println("21. Orar complet");
        System.out.println("22. Sterge student");
        System.out.println("23. Sterge curs");
        System.out.println(" 0. Iesire");
    }

    private void handle(int choice) {
        switch (choice) {
            case 1 -> addDepartment();
            case 2 -> listDepartments();
            case 3 -> addStudent();
            case 4 -> listStudents();
            case 5 -> addProfessor();
            case 6 -> listProfessors();
            case 7 -> addCourse();
            case 8 -> listCourses();
            case 9 -> assignProfessor();
            case 10 -> addClassroom();
            case 11 -> listClassrooms();
            case 12 -> enrollStudent();
            case 13 -> listEnrollmentsByCourse();
            case 14 -> addGrade();
            case 15 -> listGradesByStudent();
            case 16 -> computeAverage();
            case 17 -> markAttendance();
            case 18 -> listAttendance();
            case 19 -> addScheduleEntry();
            case 20 -> viewScheduleByCourse();
            case 21 -> scheduleService.printFullSchedule();
            case 22 -> deleteStudent();
            case 23 -> deleteCourse();
            default -> System.out.println("Optiune invalida.");
        }
    }

    private void addDepartment() {
        String name = readLine("Nume departament: ");
        String code = readLine("Cod departament: ");
        var d = departmentService.add(name, code);
        System.out.println("Creat: " + d.printDetails());
    }

    private void listDepartments() {
        departmentService.getAll().forEach(d -> System.out.println(d.printDetails()));
    }

    private void addStudent() {
        String first = readLine("Prenume: ");
        String last = readLine("Nume: ");
        String email = readLine("Email: ");
        String reg = readLine("Numar matricol: ");
        int dep = readInt("Id departament: ");
        var s = studentService.add(first, last, email, reg, dep);
        System.out.println("Creat: " + s.printDetails());
    }

    private void listStudents() {
        studentService.getAllSortedByName().forEach(s -> System.out.println(s.printDetails()));
    }

    private void addProfessor() {
        String first = readLine("Prenume: ");
        String last = readLine("Nume: ");
        String email = readLine("Email: ");
        String title = readLine("Titlu (lector/conf/prof): ");
        int dep = readInt("Id departament: ");
        var p = professorService.add(first, last, email, title, dep);
        System.out.println("Creat: " + p.printDetails());
    }

    private void listProfessors() {
        professorService.getAll().forEach(p -> System.out.println(p.printDetails()));
    }

    private void addCourse() {
        String name = readLine("Nume curs: ");
        String code = readLine("Cod curs: ");
        int credits = readInt("Credite: ");
        CourseType type = readEnum("Tip (MANDATORY/OPTIONAL/ELECTIVE): ", CourseType.class);
        int dep = readInt("Id departament: ");
        var c = courseService.add(name, code, credits, type, dep);
        System.out.println("Creat: " + c.printDetails());
    }

    private void listCourses() {
        for (Course c : courseService.getAllSortedByCode().values()) {
            System.out.println(c.printDetails());
        }
    }

    private void assignProfessor() {
        int courseId = readInt("Id curs: ");
        int profId = readInt("Id profesor: ");
        courseService.assignProfessor(courseId, profId);
        System.out.println("OK.");
    }

    private void addClassroom() {
        String name = readLine("Nume sala: ");
        int capacity = readInt("Capacitate: ");
        String building = readLine("Cladire: ");
        var c = classroomService.add(name, capacity, building);
        System.out.println("Creat: " + c.printDetails());
    }

    private void listClassrooms() {
        classroomService.getAll().forEach(c -> System.out.println(c.printDetails()));
    }

    private void enrollStudent() {
        int sId = readInt("Id student: ");
        int cId = readInt("Id curs: ");
        String year = readLine("An academic (ex: 2025-2026): ");
        var e = enrollmentService.enroll(sId, cId, year);
        System.out.println("Creat: " + e.printDetails());
    }

    private void listEnrollmentsByCourse() {
        int cId = readInt("Id curs: ");
        enrollmentService.getByCourse(cId).forEach(e -> System.out.println(e.printDetails()));
    }

    private void addGrade() {
        int sId = readInt("Id student: ");
        int cId = readInt("Id curs: ");
        double value = readDouble("Nota (1.0 - 10.0): ");
        double weight = readDouble("Pondere (0 - 1]: ");
        String type = readLine("Tip evaluare (exam/quiz/lab/...): ");
        var g = gradeService.add(sId, cId, value, weight, type);
        System.out.println("Creat: " + g.printDetails());
    }

    private void listGradesByStudent() {
        int sId = readInt("Id student: ");
        gradeService.getByStudent(sId).forEach(g -> System.out.println(g.printDetails()));
    }

    private void computeAverage() {
        int sId = readInt("Id student: ");
        int cId = readInt("Id curs: ");
        double avg = gradeService.computeWeightedAverage(sId, cId);
        System.out.printf("Media: %.2f%n", avg);
    }

    private void markAttendance() {
        int sId = readInt("Id student: ");
        int cId = readInt("Id curs: ");
        LocalDate date = LocalDate.parse(readLine("Data (YYYY-MM-DD): "));
        AttendanceStatus status = readEnum("Status (PRESENT/ABSENT/EXCUSED): ", AttendanceStatus.class);
        var a = attendanceService.mark(sId, cId, date, status);
        System.out.println("Creat: " + a.printDetails());
    }

    private void listAttendance() {
        int cId = readInt("Id curs: ");
        LocalDate date = LocalDate.parse(readLine("Data (YYYY-MM-DD): "));
        attendanceService.getByCourseAndDate(cId, date).forEach(a -> System.out.println(a.printDetails()));
    }

    private void addScheduleEntry() {
        int cId = readInt("Id curs: ");
        int rId = readInt("Id sala: ");
        WeekDay day = readEnum("Zi (MONDAY..FRIDAY): ", WeekDay.class);
        LocalTime start = LocalTime.parse(readLine("Start (HH:MM): "));
        LocalTime end = LocalTime.parse(readLine("Stop (HH:MM): "));
        var s = scheduleService.add(cId, rId, day, start, end);
        System.out.println("Creat: " + s.printDetails());
    }

    private void viewScheduleByCourse() {
        int cId = readInt("Id curs: ");
        for (Printable p : scheduleService.getByCourse(cId)) {
            System.out.println(p.printDetails());
        }
    }

    private void deleteStudent() {
        int id = readInt("Id student: ");
        studentService.delete(id);
        System.out.println("Sters.");
    }

    private void deleteCourse() {
        int id = readInt("Id curs: ");
        courseService.delete(id);
        System.out.println("Sters.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Valoare numerica invalida, incearca din nou.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Valoare numerica invalida, incearca din nou.");
            }
        }
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        while (true) {
            try {
                return Enum.valueOf(type, readLine(prompt).trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Valoare invalida, incearca din nou.");
            }
        }
    }
}
