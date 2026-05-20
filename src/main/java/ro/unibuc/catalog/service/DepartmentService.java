package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.DeleteNotAllowedException;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Department;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.ProfessorRepository;
import ro.unibuc.catalog.repository.StudentRepository;

import java.util.List;

public class DepartmentService {

    private final DepartmentRepository repository;
    private final StudentRepository students;
    private final ProfessorRepository professors;
    private final CourseRepository courses;
    private final AuditService audit;

    public DepartmentService(DepartmentRepository repository,
                             StudentRepository students,
                             ProfessorRepository professors,
                             CourseRepository courses,
                             AuditService audit) {
        this.repository = repository;
        this.students = students;
        this.professors = professors;
        this.courses = courses;
        this.audit = audit;
    }

    public Department add(String name, String code) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Department name is required");
        }
        if (code == null || code.isBlank()) {
            throw new ValidationException("Department code is required");
        }
        Department d = new Department(name.trim(), code.trim().toUpperCase());
        repository.create(d);
        audit.log("ADD_DEPARTMENT");
        return d;
    }

    public List<Department> getAll() {
        audit.log("LIST_DEPARTMENTS");
        return repository.findAll();
    }

    public Department getById(int id) {
        Department d = repository.findById(id);
        if (d == null) {
            throw new EntityNotFoundException("Department not found: " + id);
        }
        return d;
    }

    public void rename(int id, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new ValidationException("New name is required");
        }
        if (repository.findById(id) == null) {
            throw new EntityNotFoundException("Department not found: " + id);
        }
        repository.updateName(id, newName.trim());
        audit.log("UPDATE_DEPARTMENT_NAME");
    }

    public void delete(int id) {
        if (repository.findById(id) == null) {
            throw new EntityNotFoundException("Department not found: " + id);
        }
        if (students.existsByDepartment(id) || professors.existsByDepartment(id)
                || courses.existsByDepartment(id)) {
            throw new DeleteNotAllowedException(
                    "Department " + id + " still has students, professors or courses attached");
        }
        repository.delete(id);
        audit.log("DELETE_DEPARTMENT");
    }
}
