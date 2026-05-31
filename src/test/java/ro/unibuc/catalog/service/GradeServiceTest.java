package ro.unibuc.catalog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.model.Grade;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.GradeRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository grades;
    @Mock
    private StudentRepository students;
    @Mock
    private CourseRepository courses;
    @Mock
    private EnrollmentRepository enrollments;
    @Mock
    private AuditService audit;

    @InjectMocks
    private GradeService service;

    private Student student() {
        return new Student(1, "Maria", "Ionescu", "maria@stud.ro", "M-1", StudentStatus.ACTIVE, 1);
    }

    private Course course() {
        return new Course(1, "PAOJ", "PAOJ", 6, CourseType.MANDATORY, 1, null);
    }

    @Test
    void add_withValueOutOfRange_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> service.add(1, 1, 12.0, 1.0, "exam"));
    }

    @Test
    void add_whenStudentNotEnrolled_throwsValidation() {
        when(students.findById(1)).thenReturn(student());
        when(courses.findById(1)).thenReturn(course());
        when(enrollments.exists(1, 1)).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> service.add(1, 1, 9.0, 0.5, "exam"));
    }

    @Test
    void computeWeightedAverage_returnsWeightedResult() {
        when(students.findById(1)).thenReturn(student());
        when(courses.findById(1)).thenReturn(course());
        when(grades.findByStudentAndCourse(1, 1)).thenReturn(List.of(
                new Grade(1, 1, 1, 9.0, 0.4, "midterm"),
                new Grade(2, 1, 1, 8.0, 0.6, "final")
        ));

        double avg = service.computeWeightedAverage(1, 1);

        assertEquals(8.4, avg, 1e-9);
    }

    @Test
    void computeWeightedAverage_withNoGrades_throwsEntityNotFound() {
        when(students.findById(1)).thenReturn(student());
        when(courses.findById(1)).thenReturn(course());
        when(grades.findByStudentAndCourse(1, 1)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class,
                () -> service.computeWeightedAverage(1, 1));
    }
}
