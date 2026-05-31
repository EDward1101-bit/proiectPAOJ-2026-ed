package ro.unibuc.catalog.service;

import ro.unibuc.catalog.config.AppLogger;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.model.Department;
import ro.unibuc.catalog.model.Grade;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.GradeRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Read-only analytics computed in memory with the Streams API.
 *
 * <p>Each public method represents one reporting action and is built around a
 * stream pipeline (grouping, filtering, mapping, reduction).</p>
 */
public class ReportService {

    private static final Logger LOG = AppLogger.get(ReportService.class);

    private final StudentRepository students;
    private final CourseRepository courses;
    private final GradeRepository grades;
    private final DepartmentRepository departments;
    private final AuditService audit;

    public ReportService(StudentRepository students,
                         CourseRepository courses,
                         GradeRepository grades,
                         DepartmentRepository departments,
                         AuditService audit) {
        this.students = students;
        this.courses = courses;
        this.grades = grades;
        this.departments = departments;
        this.audit = audit;
    }

    /** Aggregated entry pairing a student with a computed average. */
    public record StudentAverage(Student student, double weightedAverage) {
    }

    /** Number of students grouped by their enrollment status. */
    public Map<StudentStatus, Long> studentCountByStatus() {
        audit.log("REPORT_STUDENTS_BY_STATUS");
        LOG.info("Computing student count by status");
        return students.findAll().stream()
                .collect(Collectors.groupingBy(
                        Student::getStatus,
                        () -> new TreeMap<>(Comparator.comparing(Enum::name)),
                        Collectors.counting()));
    }

    /** Number of courses offered by each department (keyed by department code). */
    public Map<String, Long> coursesPerDepartment() {
        Map<Integer, String> deptCodes = departments.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getCode));

        audit.log("REPORT_COURSES_PER_DEPARTMENT");
        LOG.info("Computing courses per department");
        return courses.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> deptCodes.getOrDefault(c.getDepartmentId(), "?"),
                        TreeMap::new,
                        Collectors.counting()));
    }

    /** Average number of credits for each course type. */
    public Map<CourseType, Double> averageCreditsByCourseType() {
        audit.log("REPORT_CREDITS_BY_TYPE");
        LOG.info("Computing average credits by course type");
        return courses.findAll().stream()
                .collect(Collectors.groupingBy(
                        Course::getType,
                        () -> new TreeMap<>(Comparator.comparing(Enum::name)),
                        Collectors.averagingInt(Course::getCredits)));
    }

    /** Courses that do not yet have a professor assigned, sorted by code. */
    public List<Course> coursesWithoutProfessor() {
        audit.log("REPORT_COURSES_WITHOUT_PROFESSOR");
        LOG.info("Listing courses without a professor");
        return courses.findAll().stream()
                .filter(c -> c.getProfessorId() == null)
                .sorted(Comparator.comparing(Course::getCode))
                .toList();
    }

    /** Simple arithmetic mean of every recorded grade value (0 when none). */
    public double overallGradeAverage() {
        audit.log("REPORT_OVERALL_AVERAGE");
        LOG.info("Computing overall grade average");
        return grades.findAll().stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);
    }

    /** Top students ranked by their weighted average across all courses. */
    public List<StudentAverage> topStudentsByAverage(int limit) {
        Map<Integer, Student> studentsById = students.findAll().stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Map<Integer, List<Grade>> gradesByStudent = grades.findAll().stream()
                .collect(Collectors.groupingBy(Grade::getStudentId));

        audit.log("REPORT_TOP_STUDENTS");
        LOG.info(() -> "Ranking top " + limit + " students by weighted average");
        return gradesByStudent.entrySet().stream()
                .filter(e -> studentsById.containsKey(e.getKey()))
                .map(e -> new StudentAverage(studentsById.get(e.getKey()), weightedAverage(e.getValue())))
                .sorted(Comparator.comparingDouble(StudentAverage::weightedAverage).reversed())
                .limit(limit)
                .toList();
    }

    private static double weightedAverage(List<Grade> studentGrades) {
        double weighted = studentGrades.stream()
                .mapToDouble(g -> g.getValue() * g.getWeight())
                .sum();
        double totalWeight = studentGrades.stream()
                .mapToDouble(Grade::getWeight)
                .sum();
        return totalWeight == 0.0 ? 0.0 : weighted / totalWeight;
    }
}
