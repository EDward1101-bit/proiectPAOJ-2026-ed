package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.DuplicateException;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Enrollment;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final CourseRepository courses;
    private final AuditService audit;

    public EnrollmentService(EnrollmentRepository enrollments,
                             StudentRepository students,
                             CourseRepository courses,
                             AuditService audit) {
        this.enrollments = enrollments;
        this.students = students;
        this.courses = courses;
        this.audit = audit;
    }

    public Enrollment enroll(int studentId, int courseId, String academicYear) {
        if (academicYear == null || academicYear.isBlank()) {
            throw new ValidationException("Academic year is required");
        }
        if (students.findById(studentId) == null) {
            throw new EntityNotFoundException("Student not found: " + studentId);
        }
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        if (enrollments.exists(studentId, courseId)) {
            throw new DuplicateException("Student is already enrolled in this course");
        }

        Enrollment e = new Enrollment(studentId, courseId, academicYear.trim());
        enrollments.create(e);
        audit.log("ENROLL_STUDENT");
        return e;
    }

    public List<Enrollment> getByCourse(int courseId) {
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        List<Enrollment> list = enrollments.findByCourse(courseId);

        Set<Integer> uniqueStudents = new HashSet<>();
        for (Enrollment e : list) {
            uniqueStudents.add(e.getStudentId());
        }
        System.out.println("Unique students enrolled: " + uniqueStudents.size());

        audit.log("LIST_ENROLLMENTS_BY_COURSE");
        return list;
    }
}
