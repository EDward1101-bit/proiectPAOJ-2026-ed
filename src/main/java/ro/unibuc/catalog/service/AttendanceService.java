package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Attendance;
import ro.unibuc.catalog.model.AttendanceStatus;
import ro.unibuc.catalog.repository.AttendanceRepository;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.time.LocalDate;
import java.util.List;

public class AttendanceService {

    private final AttendanceRepository attendances;
    private final StudentRepository students;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final AuditService audit;

    public AttendanceService(AttendanceRepository attendances,
                             StudentRepository students,
                             CourseRepository courses,
                             EnrollmentRepository enrollments,
                             AuditService audit) {
        this.attendances = attendances;
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
        this.audit = audit;
    }

    public Attendance mark(int studentId, int courseId, LocalDate date, AttendanceStatus status) {
        if (date == null) {
            throw new ValidationException("Date is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Cannot record attendance in the future");
        }
        if (status == null) {
            throw new ValidationException("Status is required");
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

        Attendance a = new Attendance(studentId, courseId, date, status);
        attendances.create(a);
        audit.log("MARK_ATTENDANCE");
        return a;
    }

    public void delete(int attendanceId) {
        if (attendances.findById(attendanceId) == null) {
            throw new EntityNotFoundException("Attendance not found: " + attendanceId);
        }
        attendances.delete(attendanceId);
        audit.log("DELETE_ATTENDANCE");
    }

    public List<Attendance> getByCourseAndDate(int courseId, LocalDate date) {
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        audit.log("LIST_ATTENDANCE_BY_COURSE_DATE");
        return attendances.findByCourseAndDate(courseId, date);
    }
}
