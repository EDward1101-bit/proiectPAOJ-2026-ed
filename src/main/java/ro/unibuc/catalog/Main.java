package ro.unibuc.catalog;

import ro.unibuc.catalog.config.DatabaseInitializer;
import ro.unibuc.catalog.model.AttendanceStatus;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.model.Printable;
import ro.unibuc.catalog.model.StudentStatus;
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
import ro.unibuc.catalog.service.ExportService;
import ro.unibuc.catalog.service.GradeService;
import ro.unibuc.catalog.service.ProfessorService;
import ro.unibuc.catalog.service.ReportService;
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
    private final ReportService reportService;
    private final ExportService exportService;

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

        this.departmentService = new DepartmentService(departmentRepo, studentRepo, professorRepo, courseRepo, audit);
        this.studentService = new StudentService(studentRepo, departmentRepo, enrollmentRepo, audit);
        this.professorService = new ProfessorService(professorRepo, departmentRepo, courseRepo, audit);
        this.courseService = new CourseService(courseRepo, departmentRepo, professorRepo, audit);
        this.classroomService = new ClassroomService(classroomRepo, scheduleRepo, audit);
        this.enrollmentService = new EnrollmentService(enrollmentRepo, studentRepo, courseRepo, audit);
        this.gradeService = new GradeService(gradeRepo, studentRepo, courseRepo, enrollmentRepo, audit);
        this.attendanceService = new AttendanceService(attendanceRepo, studentRepo, courseRepo, enrollmentRepo, audit);
        this.scheduleService = new ScheduleService(scheduleRepo, courseRepo, classroomRepo, audit);
        this.reportService = new ReportService(studentRepo, courseRepo, gradeRepo, departmentRepo, audit);
        this.exportService = new ExportService(studentRepo, reportService, audit);
    }

    public static void main(String[] args) {
        DatabaseInitializer.run();
        new Main().run();
    }

    private void run() {
        while (true) {
            printMenu();
            int choice = readInt("Choice: ");
            if (choice == 0) {
                System.out.println("Goodbye!");
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
        System.out.println("===== Academic Catalog =====");
        System.out.println("-- Departments --");
        System.out.println(" 1. Add department");
        System.out.println(" 2. List departments");
        System.out.println(" 3. Rename department");
        System.out.println(" 4. Delete department");
        System.out.println("-- Students --");
        System.out.println(" 5. Add student");
        System.out.println(" 6. List students (sorted by name)");
        System.out.println(" 7. Update student status");
        System.out.println(" 8. Delete student");
        System.out.println("-- Professors --");
        System.out.println(" 9. Add professor");
        System.out.println("10. List professors");
        System.out.println("11. Update professor title");
        System.out.println("12. Delete professor");
        System.out.println("-- Courses --");
        System.out.println("13. Add course");
        System.out.println("14. List courses (sorted by code)");
        System.out.println("15. Assign professor to course");
        System.out.println("16. Delete course");
        System.out.println("-- Classrooms --");
        System.out.println("17. Add classroom");
        System.out.println("18. List classrooms");
        System.out.println("19. Update classroom capacity");
        System.out.println("20. Delete classroom");
        System.out.println("-- Enrollments --");
        System.out.println("21. Enroll student in course");
        System.out.println("22. List enrollments for a course");
        System.out.println("23. Cancel enrollment");
        System.out.println("-- Grades --");
        System.out.println("24. Add grade");
        System.out.println("25. List grades for a student");
        System.out.println("26. Compute weighted average (student/course)");
        System.out.println("27. Delete grade");
        System.out.println("-- Attendance --");
        System.out.println("28. Record attendance");
        System.out.println("29. List attendance for a course on a date");
        System.out.println("30. Delete attendance");
        System.out.println("-- Schedule --");
        System.out.println("31. Add schedule entry");
        System.out.println("32. Schedule for a course");
        System.out.println("33. Full schedule");
        System.out.println("34. Delete schedule entry");
        System.out.println("-- Reports (Streams) --");
        System.out.println("35. Students by status");
        System.out.println("36. Courses per department");
        System.out.println("37. Average credits by course type");
        System.out.println("38. Courses without a professor");
        System.out.println("39. Overall grade average");
        System.out.println("40. Top students by weighted average");
        System.out.println("-- Export --");
        System.out.println("41. Export students to CSV");
        System.out.println("42. Export statistics to JSON");
        System.out.println(" 0. Exit");
    }

    private void handle(int choice) {
        switch (choice) {
            case 1 -> addDepartment();
            case 2 -> listDepartments();
            case 3 -> renameDepartment();
            case 4 -> deleteDepartment();
            case 5 -> addStudent();
            case 6 -> listStudents();
            case 7 -> updateStudentStatus();
            case 8 -> deleteStudent();
            case 9 -> addProfessor();
            case 10 -> listProfessors();
            case 11 -> updateProfessorTitle();
            case 12 -> deleteProfessor();
            case 13 -> addCourse();
            case 14 -> listCourses();
            case 15 -> assignProfessor();
            case 16 -> deleteCourse();
            case 17 -> addClassroom();
            case 18 -> listClassrooms();
            case 19 -> updateClassroomCapacity();
            case 20 -> deleteClassroom();
            case 21 -> enrollStudent();
            case 22 -> listEnrollmentsByCourse();
            case 23 -> cancelEnrollment();
            case 24 -> addGrade();
            case 25 -> listGradesByStudent();
            case 26 -> computeAverage();
            case 27 -> deleteGrade();
            case 28 -> markAttendance();
            case 29 -> listAttendance();
            case 30 -> deleteAttendance();
            case 31 -> addScheduleEntry();
            case 32 -> viewScheduleByCourse();
            case 33 -> scheduleService.formatFullSchedule().forEach(System.out::println);
            case 34 -> deleteScheduleEntry();
            case 35 -> reportStudentsByStatus();
            case 36 -> reportCoursesPerDepartment();
            case 37 -> reportAverageCreditsByType();
            case 38 -> reportCoursesWithoutProfessor();
            case 39 -> reportOverallAverage();
            case 40 -> reportTopStudents();
            case 41 -> exportStudentsCsv();
            case 42 -> exportStatisticsJson();
            default -> System.out.println("Invalid option.");
        }
    }

    // ------ Departments ------

    private void addDepartment() {
        String name = readLine("Department name: ");
        String code = readLine("Department code: ");
        var d = departmentService.add(name, code);
        System.out.println("Created: " + d.printDetails());
    }

    private void listDepartments() {
        departmentService.getAll().forEach(d -> System.out.println(d.printDetails()));
    }

    private void renameDepartment() {
        int id = readInt("Department id: ");
        String name = readLine("New name: ");
        departmentService.rename(id, name);
        System.out.println("Updated.");
    }

    private void deleteDepartment() {
        int id = readInt("Department id: ");
        departmentService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Students ------

    private void addStudent() {
        String first = readLine("First name: ");
        String last = readLine("Last name: ");
        String email = readLine("Email: ");
        String reg = readLine("Registration number: ");
        int dep = readInt("Department id: ");
        var s = studentService.add(first, last, email, reg, dep);
        System.out.println("Created: " + s.printDetails());
    }

    private void listStudents() {
        studentService.getAllSortedByName().forEach(s -> System.out.println(s.printDetails()));
    }

    private void updateStudentStatus() {
        int id = readInt("Student id: ");
        StudentStatus status = readEnum("New status (ACTIVE/GRADUATED/SUSPENDED/WITHDRAWN): ", StudentStatus.class);
        studentService.updateStatus(id, status);
        System.out.println("Updated.");
    }

    private void deleteStudent() {
        int id = readInt("Student id: ");
        studentService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Professors ------

    private void addProfessor() {
        String first = readLine("First name: ");
        String last = readLine("Last name: ");
        String email = readLine("Email: ");
        String title = readLine("Title (lector/conf/prof): ");
        int dep = readInt("Department id: ");
        var p = professorService.add(first, last, email, title, dep);
        System.out.println("Created: " + p.printDetails());
    }

    private void listProfessors() {
        professorService.getAll().forEach(p -> System.out.println(p.printDetails()));
    }

    private void updateProfessorTitle() {
        int id = readInt("Professor id: ");
        String title = readLine("New title: ");
        professorService.updateTitle(id, title);
        System.out.println("Updated.");
    }

    private void deleteProfessor() {
        int id = readInt("Professor id: ");
        professorService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Courses ------

    private void addCourse() {
        String name = readLine("Course name: ");
        String code = readLine("Course code: ");
        int credits = readInt("Credits: ");
        CourseType type = readEnum("Type (MANDATORY/OPTIONAL/ELECTIVE): ", CourseType.class);
        int dep = readInt("Department id: ");
        var c = courseService.add(name, code, credits, type, dep);
        System.out.println("Created: " + c.printDetails());
    }

    private void listCourses() {
        for (Course c : courseService.getAllSortedByCode().values()) {
            System.out.println(c.printDetails());
        }
    }

    private void assignProfessor() {
        int courseId = readInt("Course id: ");
        int profId = readInt("Professor id: ");
        courseService.assignProfessor(courseId, profId);
        System.out.println("OK.");
    }

    private void deleteCourse() {
        int id = readInt("Course id: ");
        courseService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Classrooms ------

    private void addClassroom() {
        String name = readLine("Classroom name: ");
        int capacity = readInt("Capacity: ");
        String building = readLine("Building: ");
        var c = classroomService.add(name, capacity, building);
        System.out.println("Created: " + c.printDetails());
    }

    private void listClassrooms() {
        classroomService.getAll().forEach(c -> System.out.println(c.printDetails()));
    }

    private void updateClassroomCapacity() {
        int id = readInt("Classroom id: ");
        int capacity = readInt("New capacity: ");
        classroomService.updateCapacity(id, capacity);
        System.out.println("Updated.");
    }

    private void deleteClassroom() {
        int id = readInt("Classroom id: ");
        classroomService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Enrollments ------

    private void enrollStudent() {
        int sId = readInt("Student id: ");
        int cId = readInt("Course id: ");
        String year = readLine("Academic year (e.g. 2025-2026): ");
        var e = enrollmentService.enroll(sId, cId, year);
        System.out.println("Created: " + e.printDetails());
    }

    private void listEnrollmentsByCourse() {
        int cId = readInt("Course id: ");
        enrollmentService.getByCourse(cId).forEach(e -> System.out.println(e.printDetails()));
        System.out.println("Unique students enrolled: " + enrollmentService.distinctStudentIds(cId).size());
    }

    private void cancelEnrollment() {
        int id = readInt("Enrollment id: ");
        enrollmentService.cancel(id);
        System.out.println("Cancelled.");
    }

    // ------ Grades ------

    private void addGrade() {
        int sId = readInt("Student id: ");
        int cId = readInt("Course id: ");
        double value = readDouble("Grade (1.0 - 10.0): ");
        double weight = readDouble("Weight (0 - 1]: ");
        String type = readLine("Evaluation type (exam/quiz/lab/...): ");
        var g = gradeService.add(sId, cId, value, weight, type);
        System.out.println("Created: " + g.printDetails());
    }

    private void listGradesByStudent() {
        int sId = readInt("Student id: ");
        gradeService.getByStudent(sId).forEach(g -> System.out.println(g.printDetails()));
    }

    private void computeAverage() {
        int sId = readInt("Student id: ");
        int cId = readInt("Course id: ");
        double avg = gradeService.computeWeightedAverage(sId, cId);
        System.out.printf("Average: %.2f%n", avg);
    }

    private void deleteGrade() {
        int id = readInt("Grade id: ");
        gradeService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Attendance ------

    private void markAttendance() {
        int sId = readInt("Student id: ");
        int cId = readInt("Course id: ");
        LocalDate date = LocalDate.parse(readLine("Date (YYYY-MM-DD): "));
        AttendanceStatus status = readEnum("Status (PRESENT/ABSENT/EXCUSED): ", AttendanceStatus.class);
        var a = attendanceService.mark(sId, cId, date, status);
        System.out.println("Created: " + a.printDetails());
    }

    private void listAttendance() {
        int cId = readInt("Course id: ");
        LocalDate date = LocalDate.parse(readLine("Date (YYYY-MM-DD): "));
        attendanceService.getByCourseAndDate(cId, date).forEach(a -> System.out.println(a.printDetails()));
    }

    private void deleteAttendance() {
        int id = readInt("Attendance id: ");
        attendanceService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Schedule ------

    private void addScheduleEntry() {
        int cId = readInt("Course id: ");
        int rId = readInt("Classroom id: ");
        WeekDay day = readEnum("Day (MONDAY..FRIDAY): ", WeekDay.class);
        LocalTime start = LocalTime.parse(readLine("Start (HH:MM): "));
        LocalTime end = LocalTime.parse(readLine("End (HH:MM): "));
        var s = scheduleService.add(cId, rId, day, start, end);
        System.out.println("Created: " + s.printDetails());
    }

    private void viewScheduleByCourse() {
        int cId = readInt("Course id: ");
        for (Printable p : scheduleService.getByCourse(cId)) {
            System.out.println(p.printDetails());
        }
    }

    private void deleteScheduleEntry() {
        int id = readInt("Schedule entry id: ");
        scheduleService.delete(id);
        System.out.println("Deleted.");
    }

    // ------ Reports (Streams API) ------

    private void reportStudentsByStatus() {
        System.out.println("Students by status:");
        reportService.studentCountByStatus()
                .forEach((status, count) -> System.out.println("  " + status + ": " + count));
    }

    private void reportCoursesPerDepartment() {
        System.out.println("Courses per department:");
        reportService.coursesPerDepartment()
                .forEach((code, count) -> System.out.println("  " + code + ": " + count));
    }

    private void reportAverageCreditsByType() {
        System.out.println("Average credits by course type:");
        reportService.averageCreditsByCourseType()
                .forEach((type, avg) -> System.out.printf("  %s: %.2f%n", type, avg));
    }

    private void reportCoursesWithoutProfessor() {
        var list = reportService.coursesWithoutProfessor();
        if (list.isEmpty()) {
            System.out.println("All courses have a professor assigned.");
            return;
        }
        System.out.println("Courses without a professor:");
        list.forEach(c -> System.out.println("  " + c.printDetails()));
    }

    private void reportOverallAverage() {
        System.out.printf("Overall grade average: %.2f%n", reportService.overallGradeAverage());
    }

    private void reportTopStudents() {
        int limit = readInt("How many students: ");
        var top = reportService.topStudentsByAverage(limit);
        if (top.isEmpty()) {
            System.out.println("No grades recorded yet.");
            return;
        }
        System.out.println("Top students by weighted average:");
        int rank = 1;
        for (var entry : top) {
            System.out.printf("  %d. %s - %.2f%n",
                    rank++, entry.student().getFullName(), entry.weightedAverage());
        }
    }

    // ------ Export ------

    private void exportStudentsCsv() {
        var file = exportService.exportStudentsToCsv();
        System.out.println("Students exported to " + file.toAbsolutePath());
    }

    private void exportStatisticsJson() {
        var file = exportService.exportStatisticsToJson();
        System.out.println("Statistics exported to " + file.toAbsolutePath());
    }

    // ------ helpers I/O ------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric value, please try again.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric value, please try again.");
            }
        }
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        while (true) {
            try {
                return Enum.valueOf(type, readLine(prompt).trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid value, please try again.");
            }
        }
    }
}
