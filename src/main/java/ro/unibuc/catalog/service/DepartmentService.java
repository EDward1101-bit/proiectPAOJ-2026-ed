package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Department;
import ro.unibuc.catalog.repository.DepartmentRepository;

import java.util.List;

public class DepartmentService {

    private final DepartmentRepository repository;
    private final AuditService audit;

    public DepartmentService(DepartmentRepository repository, AuditService audit) {
        this.repository = repository;
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
}
