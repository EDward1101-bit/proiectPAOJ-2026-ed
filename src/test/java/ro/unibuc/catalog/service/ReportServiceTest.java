package ro.unibuc.catalog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.catalog.model.Grade;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.GradeRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private StudentRepository students;
    @Mock
    private CourseRepository courses;
    @Mock
    private GradeRepository grades;
    @Mock
    private DepartmentRepository departments;
    @Mock
    private AuditService audit;

    @InjectMocks
    private ReportService service;

    private Student student(int id, StudentStatus status) {
        return new Student(id, "First" + id, "Last" + id, "s" + id + "@stud.ro", "M-" + id, status, 1);
    }

    @Test
    void studentCountByStatus_groupsAndCounts() {
        when(students.findAll()).thenReturn(List.of(
                student(1, StudentStatus.ACTIVE),
                student(2, StudentStatus.ACTIVE),
                student(3, StudentStatus.GRADUATED)
        ));

        Map<StudentStatus, Long> result = service.studentCountByStatus();

        assertEquals(2L, result.get(StudentStatus.ACTIVE));
        assertEquals(1L, result.get(StudentStatus.GRADUATED));
    }

    @Test
    void overallGradeAverage_averagesAllValues() {
        when(grades.findAll()).thenReturn(List.of(
                new Grade(1, 1, 1, 8.0, 1.0, "exam"),
                new Grade(2, 2, 1, 10.0, 1.0, "exam")
        ));

        assertEquals(9.0, service.overallGradeAverage(), 1e-9);
    }

    @Test
    void overallGradeAverage_withNoGrades_returnsZero() {
        when(grades.findAll()).thenReturn(List.of());

        assertEquals(0.0, service.overallGradeAverage(), 1e-9);
    }

    @Test
    void topStudentsByAverage_ordersByWeightedAverageDescending() {
        when(students.findAll()).thenReturn(List.of(
                student(1, StudentStatus.ACTIVE),
                student(2, StudentStatus.ACTIVE)
        ));
        when(grades.findAll()).thenReturn(List.of(
                new Grade(1, 1, 1, 6.0, 1.0, "exam"),
                new Grade(2, 2, 1, 9.0, 1.0, "exam")
        ));

        List<ReportService.StudentAverage> top = service.topStudentsByAverage(2);

        assertEquals(2, top.size());
        assertEquals(2, top.get(0).student().getId());
        assertEquals(9.0, top.get(0).weightedAverage(), 1e-9);
        assertEquals(1, top.get(1).student().getId());
    }
}
