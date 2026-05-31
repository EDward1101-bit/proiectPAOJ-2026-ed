package ro.unibuc.catalog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.catalog.exception.DeleteNotAllowedException;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.InvalidStateException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Department;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository students;
    @Mock
    private DepartmentRepository departments;
    @Mock
    private EnrollmentRepository enrollments;
    @Mock
    private AuditService audit;

    @InjectMocks
    private StudentService service;

    @Test
    void add_withValidData_persistsAndReturnsStudent() {
        when(departments.findById(1)).thenReturn(new Department(1, "Math", "MATE"));

        Student created = service.add("Maria", "Ionescu", "maria@stud.ro", "M-1", 1);

        assertEquals("Maria", created.getFirstName());
        assertEquals(StudentStatus.ACTIVE, created.getStatus());
        verify(students).create(created);
        verify(audit).log("ADD_STUDENT");
    }

    @Test
    void add_withInvalidEmail_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add("Maria", "Ionescu", "no-at-sign", "M-1", 1));
        verify(students, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void add_withUnknownDepartment_throwsEntityNotFoundException() {
        when(departments.findById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.add("Maria", "Ionescu", "maria@stud.ro", "M-1", 99));
    }

    @Test
    void delete_whenStudentEnrolled_throwsDeleteNotAllowed() {
        Student s = new Student(1, "Maria", "Ionescu", "maria@stud.ro", "M-1", StudentStatus.ACTIVE, 1);
        when(students.findById(1)).thenReturn(s);
        when(enrollments.existsByStudent(1)).thenReturn(true);

        assertThrows(DeleteNotAllowedException.class, () -> service.delete(1));
        verify(students, never()).delete(1);
    }

    @Test
    void updateStatus_toSameStatus_throwsInvalidState() {
        Student s = new Student(1, "Maria", "Ionescu", "maria@stud.ro", "M-1", StudentStatus.ACTIVE, 1);
        when(students.findById(1)).thenReturn(s);

        assertThrows(InvalidStateException.class,
                () -> service.updateStatus(1, StudentStatus.ACTIVE));
        verify(students, never()).updateStatus(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any());
    }
}
