package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Grade;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.GradeRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.List;

public class GradeService {

    private final GradeRepository grades;
    private final StudentRepository students;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final AuditService audit;

    public GradeService(GradeRepository grades,
                        StudentRepository students,
                        CourseRepository courses,
                        EnrollmentRepository enrollments,
                        AuditService audit) {
        this.grades = grades;
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
        this.audit = audit;
    }

    public Grade add(int studentId, int courseId, double value, double weight, String evaluationType) {
        if (value < 1.0 || value > 10.0) {
            throw new ValidationException("Grade must be between 1.0 and 10.0");
        }
        if (weight <= 0.0 || weight > 1.0) {
            throw new ValidationException("Weight must be in (0, 1]");
        }
        if (evaluationType == null || evaluationType.isBlank()) {
            throw new ValidationException("Evaluation type is required");
        }
        if (students.findById(studentId) == null) {
            throw new EntityNotFoundException("Student not found: " + studentId);
        }
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        if (!enrollments.exists(studentId, courseId)) {
            throw new ValidationException("Student is not enrolled in this course");
        }

        Grade g = new Grade(studentId, courseId, value, weight, evaluationType.trim());
        grades.create(g);
        audit.log("ADD_GRADE");
        return g;
    }

    public List<Grade> getByStudent(int studentId) {
        if (students.findById(studentId) == null) {
            throw new EntityNotFoundException("Student not found: " + studentId);
        }
        audit.log("LIST_GRADES_BY_STUDENT");
        return grades.findByStudent(studentId);
    }

    public double computeWeightedAverage(int studentId, int courseId) {
        if (students.findById(studentId) == null) {
            throw new EntityNotFoundException("Student not found: " + studentId);
        }
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }

        List<Grade> list = grades.findByStudentAndCourse(studentId, courseId);
        if (list.isEmpty()) {
            throw new EntityNotFoundException("No grades recorded yet");
        }

        double weighted = list.stream().mapToDouble(g -> g.getValue() * g.getWeight()).sum();
        double totalWeight = list.stream().mapToDouble(Grade::getWeight).sum();

        audit.log("COMPUTE_AVERAGE");
        return weighted / totalWeight;
    }
}
