package ro.unibuc.catalog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.catalog.exception.DuplicateException;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.model.Enrollment;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollments;
    @Mock
    private StudentRepository students;
    @Mock
    private CourseRepository courses;
    @Mock
    private AuditService audit;

    @InjectMocks
    private EnrollmentService service;

    @Test
    void enroll_whenAlreadyEnrolled_throwsDuplicate() {
        when(students.findById(1)).thenReturn(
                new Student(1, "Maria", "Ionescu", "maria@stud.ro", "M-1", StudentStatus.ACTIVE, 1));
        when(courses.findById(1)).thenReturn(
                new Course(1, "PAOJ", "PAOJ", 6, CourseType.MANDATORY, 1, null));
        when(enrollments.exists(1, 1)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.enroll(1, 1, "2025-2026"));
    }

    @Test
    void distinctStudentIds_collapsesDuplicateStudents() {
        when(courses.findById(1)).thenReturn(
                new Course(1, "PAOJ", "PAOJ", 6, CourseType.MANDATORY, 1, null));
        when(enrollments.findByCourse(1)).thenReturn(List.of(
                new Enrollment(1, 7, 1, "2025-2026"),
                new Enrollment(2, 3, 1, "2025-2026"),
                new Enrollment(3, 7, 1, "2024-2025")
        ));

        Set<Integer> ids = service.distinctStudentIds(1);

        assertEquals(Set.of(3, 7), ids);
    }
}
