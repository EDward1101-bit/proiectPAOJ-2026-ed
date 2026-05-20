package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.DeleteNotAllowedException;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.InvalidStateException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.model.StudentStatus;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.EnrollmentRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.Comparator;
import java.util.List;

public class StudentService {

    private final StudentRepository students;
    private final DepartmentRepository departments;
    private final EnrollmentRepository enrollments;
    private final AuditService audit;

    public StudentService(StudentRepository students,
                          DepartmentRepository departments,
                          EnrollmentRepository enrollments,
                          AuditService audit) {
        this.students = students;
        this.departments = departments;
        this.enrollments = enrollments;
        this.audit = audit;
    }

    public Student add(String firstName, String lastName, String email,
                       String registrationNumber, int departmentId) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("Student name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Invalid email");
        }
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new ValidationException("Registration number is required");
        }
        if (departments.findById(departmentId) == null) {
            throw new EntityNotFoundException("Department not found: " + departmentId);
        }

        Student s = new Student(firstName.trim(), lastName.trim(), email.trim(),
                registrationNumber.trim(), departmentId);
        students.create(s);
        audit.log("ADD_STUDENT");
        return s;
    }

    public List<Student> getAll() {
        audit.log("LIST_STUDENTS");
        return students.findAll();
    }

    public List<Student> getAllSortedByName() {
        List<Student> list = students.findAll();
        list.sort(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName));
        audit.log("LIST_STUDENTS_SORTED");
        return list;
    }

    public Student getById(int id) {
        Student s = students.findById(id);
        if (s == null) {
            throw new EntityNotFoundException("Student not found: " + id);
        }
        return s;
    }

    public void delete(int id) {
        if (students.findById(id) == null) {
            throw new EntityNotFoundException("Student not found: " + id);
        }
        if (enrollments.existsByStudent(id)) {
            throw new DeleteNotAllowedException(
                    "Student " + id + " is enrolled in courses and cannot be deleted");
        }
        students.delete(id);
        audit.log("DELETE_STUDENT");
    }

    public void updateStatus(int id, StudentStatus status) {
        Student s = students.findById(id);
        if (s == null) {
            throw new EntityNotFoundException("Student not found: " + id);
        }
        if (status == null) {
            throw new ValidationException("Status is required");
        }
        if (s.getStatus() == status) {
            throw new InvalidStateException("Student already has status " + status);
        }
        students.updateStatus(id, status);
        audit.log("UPDATE_STUDENT_STATUS");
    }
}
